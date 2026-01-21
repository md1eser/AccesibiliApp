package com.accesibilidad.accesibiliapp.vistas.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.accesibilidad.accesibiliapp.data.deteccion.DetectionResult
import com.accesibilidad.accesibiliapp.data.deteccion.Detector
import com.accesibilidad.accesibiliapp.data.repository.CaptureRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val detector: Detector
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    // --- Estados Privados (Mutable) ---
    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    private val _isDetectorInitialized = MutableStateFlow(false)
    private val _inferenceTime = MutableStateFlow(0L)

    // Canal para eventos de "una sola vez" (Navegación)
    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)

    // Variables internas de control
    private var detectionJob: Job? = null
    private var lastDetectionResult: DetectionResult? = null

    init {
        // 1. Inicializar el detector
        viewModelScope.launch {
            try {
                Log.d(TAG, "Inicializando detector...")
                detector.initialize()
                _isDetectorInitialized.value = true
                Log.d(TAG, "Detector inicializado correctamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error inicializando detector", e)
            }
        }

        // 2. Suscribirse a los resultados del detector
        detector.results
            .onEach { result ->
                Log.d(TAG, "========== DETECTION RESULT ==========")
                Log.d(TAG, "Bitmap dimensions: ${result.bitmap.width} x ${result.bitmap.height}")
                Log.d(TAG, "Inference time: ${result.inferenceTime}ms")
                Log.d(TAG, "Number of bounding boxes: ${result.boundingBoxes?.size ?: 0}")

                result.boundingBoxes?.forEachIndexed { index, box ->
                    Log.d(TAG, "--- BoundingBox[$index] ---")
                    Log.d(TAG, "  Class: ${box.clsName}")
                    Log.d(TAG, "  Confidence: ${String.format("%.4f", box.cnf)}")
                    Log.d(TAG, "  Position: x1=${box.x1}, y1=${box.y1}")
                    Log.d(TAG, "  Size: w=${box.w}, h=${box.h}")
                    Log.d(TAG, "  Bounds: x2=${box.x1 + box.w}, y2=${box.y1 + box.h}")

                    // Verificar si las coordenadas están dentro de la imagen
                    val isValid = box.x1 >= 0 && box.y1 >= 0 &&
                            (box.x1 + box.w) <= result.bitmap.width &&
                            (box.y1 + box.h) <= result.bitmap.height
                    Log.d(TAG, "  Valid coordinates: $isValid")
                }
                Log.d(TAG, "======================================")

                _detectionResult.value = result
                _inferenceTime.value = result.inferenceTime
                lastDetectionResult = result
            }
            .launchIn(viewModelScope)
    }

    // --- MÉTODOS PÚBLICOS (API EXACTA PARA LA UI) ---

    fun getDetectionResult(): StateFlow<DetectionResult?> {
        return _detectionResult.asStateFlow()
    }

    fun getNavigationEvent(): Flow<Unit> {
        return _navigationEvent.receiveAsFlow()
    }

    fun isDetectorInitialized(): StateFlow<Boolean> = _isDetectorInitialized.asStateFlow()

    fun getInferenceTime(): StateFlow<Long> = _inferenceTime.asStateFlow()

    // --- LÓGICA DE NEGOCIO ---

    fun processFrame(bitmap: Bitmap, triggerNavigation: Boolean = false) {
        // Control de concurrencia: si hay un trabajo activo, saltamos este frame
        if (detectionJob?.isActive == true) {
            Log.v(TAG, "Frame skipped - detection job already active")
            return
        }

        Log.v(TAG, "Processing frame: ${bitmap.width}x${bitmap.height}")

        detectionJob = viewModelScope.launch {
            try {
                // Verificar si el detector necesita reinicio
                if (!detector.isActive()) {
                    Log.d(TAG, "Detector inactive - restarting")
                    detector.restart()
                }

                // Ejecutar detección
                detector.detect(bitmap)

                // Si fue solicitado (ej. imagen estática), navegar al terminar
                if (triggerNavigation) {
                    Log.d(TAG, "Navigation trigger requested")
                    onCaptureRequest()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame", e)
            }
        }
    }

    fun onCaptureRequest() {
        val result = lastDetectionResult
        if (result != null) {
            Log.d(TAG, "========== CAPTURE REQUEST ==========")
            Log.d(TAG, "Captured bitmap: ${result.bitmap.width}x${result.bitmap.height}")
            Log.d(TAG, "Captured bounding boxes: ${result.boundingBoxes?.size ?: 0}")
            Log.d(TAG, "=====================================")

            captureRepository.setDetectionResult(result)

            // Enviamos el evento
            viewModelScope.launch {
                _navigationEvent.send(Unit)
            }
        } else {
            Log.w(TAG, "Capture request failed - no detection result available")
        }
    }

    fun resetCaptureRepository() {
        Log.d(TAG, "Resetting capture repository")
        captureRepository.clear()
        _detectionResult.value = null
        lastDetectionResult = null
    }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        Log.d(TAG, "========== BITMAP ROTATION ==========")
        Log.d(TAG, "Original size: ${source.width}x${source.height}")
        Log.d(TAG, "Rotation angle: ${angle} degrees")

        val matrix = Matrix()
        matrix.postRotate(angle)
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

        Log.d(TAG, "Rotated size: ${rotated.width}x${rotated.height}")
        Log.d(TAG, "=====================================")

        return rotated
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel cleared - closing detector")
        detector.close()
        super.onCleared()
    }
}