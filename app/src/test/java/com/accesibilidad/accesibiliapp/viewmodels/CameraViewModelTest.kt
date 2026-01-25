package com.accesibilidad.accesibiliapp.viewmodels

import android.graphics.Bitmap
import android.os.Build
import com.accesibilidad.accesibiliapp.data.deteccion.DetectionResult
import com.accesibilidad.accesibiliapp.data.deteccion.Detector
import com.accesibilidad.accesibiliapp.data.repository.CaptureRepository
import com.accesibilidad.accesibiliapp.util.MainDispatcherRule
import com.accesibilidad.accesibiliapp.vistas.camera.CameraViewModel
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotEquals

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // O sdk = [33], define una versión estable
class CameraViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var captureRepository: CaptureRepository
    private lateinit var detector: Detector
    private lateinit var viewModel: CameraViewModel

    private val detectorResultsFlow = MutableSharedFlow<DetectionResult>()

    @Before
    fun setUp() {
        captureRepository = mockk(relaxed = true)
        detector = mockk(relaxed = true)

        // NOTA: Con Robolectric NO hace falta mockear Log.
        // Robolectric redirige Log.d, Log.e, etc. a stdout automáticamente.

        // Configuración del Flow y del Detector
        every { detector.results } returns detectorResultsFlow
        coEvery { detector.initialize() } just Runs

        viewModel = CameraViewModel(captureRepository, detector)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // --- TEST 1: INICIALIZACIÓN ---
    @Test
    fun testInitInitializesDetector() = runTest {
        coVerify(exactly = 1) { detector.initialize() }
        val isInitialized = viewModel.isDetectorInitialized().value
        assertTrue("El estado debe reflejar que el detector está listo", isInitialized)
    }

    // --- TEST 2: FLUJO DE DATOS ---
    @Test
    fun testDetectorEmissionsUpdateState() = runTest {
        // En lugar de mockk<Bitmap>, creamos uno real gracias a Robolectric
        val realBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val dummyResult = DetectionResult(emptyList(), 150L, realBitmap)

        detectorResultsFlow.emit(dummyResult)

        val currentState = viewModel.getDetectionResult().first()
        val currentInference = viewModel.getInferenceTime().first()

        assertEquals("El ViewModel debe propagar el resultado exacto", dummyResult, currentState)
        assertEquals("El tiempo de inferencia debe actualizarse", 150L, currentInference)
    }

    // --- TEST 3: FRAME DROPPING (Concurrencia) ---
    @Test
    fun testProcessFrameDropsFramesWhenBusy() = runTest {
        // Bitmaps reales
        val bitmap1 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        coEvery { detector.detect(any()) } coAnswers {
            delay(500)
        }
        every { detector.isActive() } returns true

        viewModel.processFrame(bitmap1) // Entra
        viewModel.processFrame(bitmap2) // Debería ser ignorado

        testScheduler.advanceTimeBy(600)

        coVerify(exactly = 1) { detector.detect(any()) }
    }

    // --- TEST 4: RESILIENCIA ---
    @Test
    fun testProcessFrameRestartsDetector() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        every { detector.isActive() } returns false
        coEvery { detector.restart() } just Runs

        viewModel.processFrame(bitmap)

        coVerifyOrder {
            detector.restart()
            detector.detect(bitmap)
        }
    }

    // --- TEST 5: NAVEGACIÓN Y GUARDADO ---
    @Test
    fun testOnCaptureRequestSavesData() = runTest {
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val result = DetectionResult(emptyList(), 100L, mockBitmap)

        detectorResultsFlow.emit(result)

        viewModel.onCaptureRequest()

        coVerify { captureRepository.setDetectionResult(result) }

        val event = viewModel.getNavigationEvent().first()
        assertEquals(Unit, event)
    }

    @Test
    fun testOnCaptureRequestIgnoredWhenResultNull() = runTest {
        viewModel.onCaptureRequest()
        coVerify(exactly = 0) { captureRepository.setDetectionResult(any()) }
    }

    // --- TEST 6: AHORA FUNCIONA REAL (Sin Mock Static) ---
    @Test
    fun testRotateBitmapCallsAndroidLogic() {
        // En Robolectric, Bitmap y Matrix funcionan DE VERDAD.
        // No hace falta mockear Bitmap.createBitmap.

        // Creamos un Bitmap de 100x200
        val source = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888)

        // Ejecutamos la rotación real de tu ViewModel (90 grados)
        val result = viewModel.rotateBitmap(source, 90f)

        // Verificamos que las dimensiones se hayan invertido (Rotación real)
        assertEquals("El ancho debería ser 200 tras rotar 90 grados", 200, result.width)
        assertEquals("El alto debería ser 100 tras rotar 90 grados", 100, result.height)

        // Opcional: Verificar que no sea el mismo objeto si se creó uno nuevo
        assertNotEquals(source, result)
    }
}