package com.accesibilidad.accesibiliapp.vistas.curation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox
import com.accesibilidad.accesibiliapp.data.deteccion.DetectionResult
import com.accesibilidad.accesibiliapp.data.repository.CaptureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BoundingBoxCurationViewModel @Inject constructor(
    private val captureRepository: CaptureRepository
) : ViewModel() {

    val originalDetectionResult: StateFlow<DetectionResult?> = captureRepository.detectionResult

    private val _inactiveBoxes = MutableStateFlow<Set<BoundingBox>>(emptySet())
    val inactiveBoxes: StateFlow<Set<BoundingBox>> = _inactiveBoxes.asStateFlow()

    fun toggleBoundingBox(box: BoundingBox) {
        _inactiveBoxes.update { currentSet ->
            if (currentSet.contains(box)) {
                currentSet - box
            } else {
                currentSet + box
            }
        }
    }

    fun confirmCuration() {
        val currentResult = originalDetectionResult.value ?: return

        // Filtramos las cajas que NO están en la lista de inactivas
        val activeBoxes = currentResult.boundingBoxes.filter { box ->
            !_inactiveBoxes.value.contains(box)
        }

        // Creamos una copia del resultado con las nuevas cajas y guardamos
        val updatedResult = currentResult.copy(boundingBoxes = activeBoxes)
        captureRepository.setDetectionResult(updatedResult)
    }
}