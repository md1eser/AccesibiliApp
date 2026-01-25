package com.accesibilidad.accesibiliapp.repository;

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.accesibilidad.accesibiliapp.data.AppDatabase
import com.accesibilidad.accesibiliapp.data.entity.CategoryEntity
import com.accesibilidad.accesibiliapp.data.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) // <--- El motor que levanta el SQL en tu PC
@Config(sdk = [33])
class CategoryRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Creamos la BD en memoria. Es rápida y se borra al terminar cada test.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Permitimos queries en el hilo principal para testear fácil
            .build()

        repository = CategoryRepository(db.categoryDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndGetAll() = runTest {
        // --- 1. INSERTAR (Probamos la función insert) ---
        val cat1 = CategoryEntity(id = 1, name = "Baño", parentId = null)
        val cat2 = CategoryEntity(id = 2, name = "Cocina", parentId = null)

        repository.insert(cat1)
        repository.insert(cat2)

        // --- 2. TRAER TODO (Probamos getAllCategories) ---
        val result = repository.getAllCategories().first()

        // --- 3. VERIFICAR ---
        assertEquals("Debería haber 2 categorías insertadas", 2, result.size)
        assertTrue(result.any { it.name == "Baño" })
        assertTrue(result.any { it.name == "Cocina" })
    }

    @Test
    fun testUpdateCategory() = runTest {
        // --- GIVEN ---
        val original = CategoryEntity(id = 10, name = "Nombre Viejo", parentId = null)
        repository.insert(original)

        // --- WHEN (Probamos updateCategory) ---
        val updated = original.copy(name = "Nombre Nuevo y Mejorado")
        repository.updateCategory(updated)

        // --- THEN ---
        // Buscamos en la BD para ver si cambió posta
        val result = repository.getAllCategories().first().find { it.id == 10L }

        assertNotNull(result)
        assertEquals("Nombre Nuevo y Mejorado", result?.name)
    }

    @Test
    fun testDeleteCategory() = runTest {
        // --- GIVEN ---
        val cat = CategoryEntity(id = 20, name = "Borrarme", parentId = null)
        repository.insert(cat)

        // Verificar que existe antes de borrar
        assertFalse(repository.getAllCategories().first().isEmpty())

        // --- WHEN (Probamos deleteCategory) ---
        repository.deleteCategory(cat)

        // --- THEN ---
        val result = repository.getAllCategories().first()
        assertTrue("La lista debería estar vacía después de borrar", result.isEmpty())
    }

    @Test
    fun testGetRootCategoriesOnlyReturnsParents() = runTest {
        // --- GIVEN (Escenario Mixto) ---
        // Root 1
        repository.insert(CategoryEntity(id = 1, name = "Edificio", parentId = null))
        // Root 2
        repository.insert(CategoryEntity(id = 2, name = "Exterior", parentId = null))

        // Hija (No debería salir en getRootCategories)
        repository.insert(CategoryEntity(id = 3, name = "Baño PB", parentId = 1))

        // --- WHEN ---
        val roots = repository.getRootCategories().first()

        // --- THEN ---
        assertEquals("Solo debe traer las 2 categorías raíz", 2, roots.size)

        // Verificamos que NINGUNA tenga parentId (null)
        roots.forEach {
            assertNull("Una categoría root no debe tener padre", it.parentId)
        }
    }

    @Test
    fun testGetSubcategoriesReturnsOnlyChildrenOfSpecificParent() = runTest {
        // --- GIVEN ---
        val idPadreTarget = 100L
        val idPadreOtro = 200L

        // Padre Target
        repository.insert(CategoryEntity(id = idPadreTarget, name = "Padre Target", parentId = null))

        // Hijos del Target (Estos son los que queremos)
        repository.insert(CategoryEntity(id = 1, name = "Hijo A", parentId = idPadreTarget))
        repository.insert(CategoryEntity(id = 2, name = "Hijo B", parentId = idPadreTarget))

        // Hijos de OTRO padre (Estos NO deben salir)
        repository.insert(CategoryEntity(id = 3, name = "Hijo Colado", parentId = idPadreOtro))

        // --- WHEN ---
        val hijos = repository.getSubcategories(idPadreTarget).first()

        // --- THEN ---
        assertEquals("Debería traer solo los 2 hijos del padre target", 2, hijos.size)

        // Validamos que sean los correctos
        assertTrue(hijos.any { it.name == "Hijo A" })
        assertTrue(hijos.any { it.name == "Hijo B" })
        assertFalse("No debería traer hijos de otro padre", hijos.any { it.name == "Hijo Colado" })
    }
}