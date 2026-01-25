package com.accesibilidad.accesibiliapp.data.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.accesibilidad.accesibiliapp.data.AppDatabase
import com.accesibilidad.accesibiliapp.data.entity.HeuristicEntity
import com.accesibilidad.accesibiliapp.data.heuristicas.ExistenceHeuristic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HeuristicRepositorySQLTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: HeuristicRepository
    private lateinit var dao: com.accesibilidad.accesibiliapp.data.dao.HeuristicDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // BD en Memoria Real
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.heuristicDao()
        repository = HeuristicRepository(dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAddSerializesPolymorphicDataCorrectly() = runTest {
        // --- GIVEN ---
        val heuristic = ExistenceHeuristic(
            id = "h_btn_search",
            name = "Existencia de Botón Buscar",
            desc = "El botón debe estar presente en la home",
            targets = listOf("icon_search", "btn_search_text"),
            scorePresent = 10.0f,
            scoreMissing = 0.0f
        )

        // --- WHEN ---
        // Guardamos usando el repositorio (esto dispara la serialización)
        repository.add(heuristic)

        // --- THEN ---
        // 1. Verificamos que se guardó en la BD
        val savedEntity = dao.getById(heuristic.id).first()
        assertNotNull("La entidad debe existir en la BD", savedEntity)

        // 2. Verificamos el contenido RAW del JSON en la base de datos
        // Esto asegura que el repositorio escribió el discriminador de tipo ("existencia")
        val jsonContent = savedEntity!!.heuristicJson

        assertTrue(
            "El JSON guardado en la BD debe contener el discriminador 'existencia'",
            jsonContent.contains("\"existencia\"")
        )
        assertTrue(jsonContent.contains("icon_search"))
        assertTrue(jsonContent.contains("10.0"))
    }

    @Test
    fun testGetAllFiltersCorruptedDataAndLogsErrors() = runTest {
        // --- GIVEN ---
        // Inyectamos datos DIRECTAMENTE al DAO para simular una BD corrupta o vieja.

        // 1. JSON Válido
        val validJson = """
            {
                "heuristicType": "existencia",
                "id": "valid_1",
                "name": "Valid Heuristic",
                "desc": "Description",
                "targets": ["targetA"],
                "scorePresent": 5.0,
                "scoreMissing": 0.0
            }
        """.trimIndent()
        dao.insert(HeuristicEntity("valid_1", validJson))

        // 2. JSON Roto (Sintaxis inválida)
        val brokenJson = "{ \"id\": \"broken\", \"heuristicType\": "
        dao.insert(HeuristicEntity("broken_1", brokenJson))

        // 3. JSON Desconocido (Tipo polimórfico no registrado)
        val unknownTypeJson = """
            {
                "heuristicType": "tipo_alienigena",
                "id": "unknown_1",
                "name": "Future"
            }
        """.trimIndent()
        dao.insert(HeuristicEntity("unknown_1", unknownTypeJson))

        // --- WHEN ---
        // El repositorio intenta leer y deserializar todo
        val resultList = repository.getAll().first()

        // --- THEN ---
        assertEquals("Debería filtrar los 2 errores y devolver solo el válido", 1, resultList.size)

        val recovered = resultList.first()
        assertTrue(recovered is ExistenceHeuristic)
        assertEquals("Valid Heuristic", recovered.name)

        // Opcional: Verificar logs con ShadowLog de Robolectric
        // Esto confirma que se imprimieron errores en el Logcat del sistema
        val logs = ShadowLog.getLogsForTag("HeuristicRepository")
        assertTrue("Debe haber logs de error", logs.any { it.type == Log.ERROR })
    }

    @Test
    fun testGetHeuristicMaintainsFloatIntegrity() = runTest {
        // --- GIVEN ---
        val id = "float_test"
        val original = ExistenceHeuristic(
            id = id,
            name = "Float Test",
            desc = "Testing floats",
            targets = emptyList(),
            scorePresent = 9.55f,  // Valor delicado
            scoreMissing = 1.2345f // Valor delicado
        )

        // --- WHEN ---
        // Ciclo completo: Serializar (Add) -> BD -> Deserializar (Get)
        repository.add(original)
        val result = repository.getHeuristic(id).first()

        // --- THEN ---
        assertNotNull(result)
        val casted = result as ExistenceHeuristic

        // Verificamos que no hubo redondeo extraño en el viaje a String JSON y vuelta
        assertEquals(9.55f, casted.scorePresent, 0.0001f)
        assertEquals(1.2345f, casted.scoreMissing, 0.0001f)
    }

    @Test
    fun testDeleteRemovesDataFromDb() = runTest {
        // --- GIVEN ---
        val heuristic = ExistenceHeuristic(
            id = "delete_me",
            name = "To Delete",
            desc = "...",
            targets = emptyList(),
            scorePresent = 0f,
            scoreMissing = 0f
        )
        repository.add(heuristic)

        // Confirmar existencia previa
        assertNotNull(dao.getById("delete_me").first())

        // --- WHEN ---
        repository.delete(heuristic)

        // --- THEN ---
        val result = dao.getById("delete_me").first()
        assertNull("La entidad debe haber desaparecido de la BD", result)
    }

    @Test
    fun testUpdateModifiesExistingData() = runTest {
        // --- GIVEN ---
        val id = "update_test"
        val original = ExistenceHeuristic(id, "Nombre Viejo", "Desc", emptyList(), 0f, 0f)
        repository.add(original)

        // --- WHEN ---
        val updated = original.copy(name = "Nombre Nuevo")
        repository.update(updated)

        // --- THEN ---
        // Leemos desde el repositorio para verificar que deserializa lo nuevo
        val result = repository.getHeuristic(id).first() as ExistenceHeuristic
        assertEquals("Nombre Nuevo", result.name)
    }
}