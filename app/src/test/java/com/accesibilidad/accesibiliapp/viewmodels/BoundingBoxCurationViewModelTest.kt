package com.accesibilidad.accesibiliapp.viewmodels

import android.graphics.Bitmap
import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox
import com.accesibilidad.accesibiliapp.data.deteccion.DetectionResult
import com.accesibilidad.accesibiliapp.data.repository.CaptureRepository
import com.accesibilidad.accesibiliapp.util.MainDispatcherRule
import com.accesibilidad.accesibiliapp.vistas.curation.BoundingBoxCurationViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BoundingBoxCurationViewModelTest {

    // 1. Regla para Corrutinas (Reutiliza la que creamos en el paso anterior)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 2. Mocks
    private lateinit var repository: CaptureRepository

    // 3. System Under Test
    private lateinit var viewModel: BoundingBoxCurationViewModel

    // Flow simulado del repositorio
    private val repoDetectionFlow = MutableStateFlow<DetectionResult?>(null)

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)

        // IMPORTANTE: Como el ViewModel lee 'detectionResult' en su inicialización (init),
        // debemos configurar el mock ANTES de instanciar el ViewModel.
        every { repository.detectionResult } returns repoDetectionFlow

        viewModel = BoundingBoxCurationViewModel(repository)
    }

    @Test
    fun testToggleBoundingBoxAddsAndRemovesFromSet() = runTest {
        // --- GIVEN ---
        val box = createDummyBox(id = 1)

        // Estado inicial vacío
        assertTrue(viewModel.inactiveBoxes.value.isEmpty())

        // --- WHEN 1: Toglear (Seleccionar para descartar) ---
        viewModel.toggleBoundingBox(box)

        // --- THEN 1 ---
        assertTrue("La caja debe estar en la lista de inactivas", viewModel.inactiveBoxes.value.contains(box))
        assertEquals(1, viewModel.inactiveBoxes.value.size)

        // --- WHEN 2: Toglear de nuevo (Deseleccionar) ---
        viewModel.toggleBoundingBox(box)

        // --- THEN 2 ---
        assertTrue("La lista debe volver a estar vacía", viewModel.inactiveBoxes.value.isEmpty())
    }

    @Test
    fun testConfirmCurationFiltersInactiveBoxesAndSaves() = runTest {
        // --- GIVEN ---
        // 1. Preparamos un resultado original con 3 cajas: A (mantener), B (borrar), C (mantener)
        val boxA = createDummyBox(1)
        val boxB = createDummyBox(2)
        val boxC = createDummyBox(3)

        val originalResult = DetectionResult(
            boundingBoxes = listOf(boxA, boxB, boxC),
            inferenceTime = 100L,
            bitmap = mockk<Bitmap>() // Mockeamos el bitmap porque no nos importa acá
        )

        // Emitimos este valor desde el repositorio simulado
        repoDetectionFlow.value = originalResult

        // 2. Marcamos la caja B como "inactiva" (para borrar)
        viewModel.toggleBoundingBox(boxB)

        // --- WHEN ---
        viewModel.confirmCuration()

        // --- THEN ---
        // Verificamos que se llame a setDetectionResult con una COPIA filtrada
        val slot = slot<DetectionResult>()
        verify(exactly = 1) { repository.setDetectionResult(capture(slot)) }

        val savedResult = slot.captured
        assertEquals("Deben quedar solo 2 cajas", 2, savedResult.boundingBoxes.size)
        assertTrue("Debe contener A", savedResult.boundingBoxes.contains(boxA))
        assertTrue("Debe contener C", savedResult.boundingBoxes.contains(boxC))
        assertFalse("NO debe contener B", savedResult.boundingBoxes.contains(boxB))
    }

    @Test
    fun testConfirmCurationDoesNothingIfResultIsNull() = runTest {
        // --- GIVEN ---
        // El repositorio devuelve null (no hubo detección aún)
        repoDetectionFlow.value = null

        // --- WHEN ---
        viewModel.confirmCuration()

        // --- THEN ---
        // No debe intentar guardar nada
        verify(exactly = 0) { repository.setDetectionResult(any()) }
    }

    // --- Helper para crear cajas rápido ---
    private fun createDummyBox(id: Int): BoundingBox {
        // Usamos valores dummy, lo único que importa para el Set es la igualdad del objeto
        return BoundingBox(
            x1 = id.toFloat(), y1 = 0f, x2 = 10f, y2 = 10f,
            cx = 5f, cy = 5f, w = 10f, h = 10f,
            cnf = 0.9f, cls = 1, clsName = "dummy_$id"
        )
    }
}