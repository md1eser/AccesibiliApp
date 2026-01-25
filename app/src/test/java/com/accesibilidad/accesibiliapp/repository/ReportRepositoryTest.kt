package com.accesibilidad.accesibiliapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.accesibilidad.accesibiliapp.data.AppDatabase
import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox
import com.accesibilidad.accesibiliapp.data.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ReportRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ReportRepository
    private lateinit var categoryRepo: CategoryRepository
    private lateinit var reportDao: com.accesibilidad.accesibiliapp.data.dao.ReportDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // BD en Memoria Real
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        reportDao = db.reportDao()
        repository = ReportRepository(reportDao)
        categoryRepo = CategoryRepository(db.categoryDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReportPersistsMetadataAndIssuesCorrectly() = runTest {
        // --- GIVEN ---
        val reportId = 1L
        val metadata = ReportMetadata(
            id = reportId,
            name = "Test Report",
            image = byteArrayOf(),
            categoryId = null
        )

        val issue = Issue(
            id = 0,
            reportId = reportId,
            type = "existencia",
            desc = "Falta rampa",
            score = 5.0f
        )

        // Creamos el BoundingBox
        val box = BoundingBox(
            x1 = 10f, y1 = 10f, x2 = 100f, y2 = 100f,
            cx = 55f, cy = 55f, w = 90f, h = 90f,
            cnf = 0.95f,
            cls = 1,
            clsName = "rampa"
        )

        // Usamos el constructor secundario (@Ignore) que acepta el BoundingBox
        val barrier = Barrier(
            issueId = 0,
            boundingBox = box
        )

        val entity = ReportEntity(metadata, listOf(IssueWithBarriers(issue, listOf(barrier))))

        // --- WHEN ---
        val insertedId = repository.insertReport(entity)

        // --- THEN ---
        assertEquals("El ID retornado debe coincidir", reportId, insertedId)

        // Verificamos en BD real
        val savedReport = reportDao.getReport(reportId).first()
        assertNotNull("El reporte debe existir en la BD", savedReport)

        val savedIssue = savedReport?.issues?.first()

        // Verificamos score del Issue
        assertEquals("El score debe persistir", 5.0f, savedIssue?.issue?.score)

        // Verificamos la Barrera
        val savedBarrier = savedIssue?.barriers?.first()
        assertNotNull(savedBarrier)

        // CORRECCIÓN AQUÍ:
        // Como Barrier es una Entity aplanada, accedemos directamente a la propiedad clsName,
        // o usamos el helper toBoundingBox() si queremos reconstruir el objeto.
        assertEquals("La clase detectada debe coincidir", "rampa", savedBarrier?.clsName)

        // También podemos probar que tu helper 'toBoundingBox' funciona
        assertEquals(0.95f, savedBarrier?.toBoundingBox()?.cnf)
    }

    @Test
    fun testUpdateReportModifiesMetadataButPreservesIssues() = runTest {
        // --- GIVEN ---
        val id = 10L
        val issue = Issue(0, id, "tipo", "desc", 10f)
        val initialEntity = ReportEntity(
            ReportMetadata(id, "Nombre Viejo", byteArrayOf(), null),
            listOf(IssueWithBarriers(issue, emptyList()))
        )
        repository.insertReport(initialEntity)

        // --- WHEN ---
        val updatedEntity = ReportEntity(
            ReportMetadata(id, "Nombre Nuevo", byteArrayOf(), null),
            emptyList()
        )
        repository.updateReport(updatedEntity)

        // --- THEN ---
        val result = reportDao.getReport(id).first()!!

        assertEquals("El nombre debió cambiar", "Nombre Nuevo", result.metadata.name)
        assertEquals("Los issues NO debieron borrarse", 1, result.issues.size)
    }

    @Test
    fun testDeleteReportRemovesDataFromDb() = runTest {
        // --- GIVEN ---
        val id = 20L
        val entity = ReportEntity(ReportMetadata(id, "Borrar", byteArrayOf(), null), emptyList())
        repository.insertReport(entity)

        assertNotNull(reportDao.getReport(id).first())

        // --- WHEN ---
        repository.deleteReport(entity)

        // --- THEN ---
        assertNull("El reporte ya no debe existir", reportDao.getReport(id).first())
    }

    @Test
    fun testDeeplyNestedTreeAverageFlatStrategy() = runTest {
        // --- ESTRUCTURA ---
        // Root (1)
        // ├── Sub A (2) -> Reporte (10.0)
        // └── Sub B (3) -> Reporte (20.0)
        //     └── Sub C (4) -> Reporte (30.0)

        categoryRepo.insert(CategoryEntity(id = 1, name = "Edificio Central", parentId = null))
        categoryRepo.insert(CategoryEntity(id = 2, name = "Baños", parentId = 1))
        categoryRepo.insert(CategoryEntity(id = 3, name = "Entrada", parentId = 1))
        categoryRepo.insert(CategoryEntity(id = 4, name = "Rampa", parentId = 3))

        createReportInDb(id = 101, catId = 2, score = 10f)
        createReportInDb(id = 102, catId = 3, score = 20f)
        createReportInDb(id = 103, catId = 4, score = 30f)

        val flow = repository.getAverageScoreForCategoryTree(1L)
        val result = flow.first()

        assertEquals("El promedio debe ser plano e incluir todos los niveles", 20.0f, result, 0.01f)
    }

    private suspend fun createReportInDb(id: Long, catId: Long, score: Float) {
        val metadata = ReportMetadata(id = id, name = "R-$id", image = byteArrayOf(), categoryId = catId)
        val issue = Issue(id = 0, reportId = id, score = score, type = "test", desc = "desc")
        val entity = ReportEntity(metadata, listOf(IssueWithBarriers(issue, emptyList())))
        repository.insertReport(entity)
    }
}