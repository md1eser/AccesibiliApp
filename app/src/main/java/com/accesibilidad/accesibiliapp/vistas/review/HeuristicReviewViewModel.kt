package com.accesibilidad.accesibiliapp.vistas.review

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.accesibilidad.accesibiliapp.data.deteccion.DetectionResult
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import com.accesibilidad.accesibiliapp.data.entity.ReportEntity
import com.accesibilidad.accesibiliapp.data.entity.ReportMetadata
import com.accesibilidad.accesibiliapp.data.repository.CaptureRepository
import com.accesibilidad.accesibiliapp.data.repository.HeuristicRepository
import com.accesibilidad.accesibiliapp.data.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HeuristicReviewViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val heuristicsRepository: HeuristicRepository,
    private val reportRepository: ReportRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 1. Detección cruda (tiene los BoundingBoxes y el Bitmap)
    val detectionResult: StateFlow<DetectionResult?> = captureRepository.detectionResult
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 2. Issues descartados manualmente por el usuario
    private val _dismissedIssues = MutableStateFlow<Set<IssueWithBarriers>>(emptySet())
    val dismissedIssues = _dismissedIssues.asStateFlow()

    // 3. LOGICA CENTRAL: Cálculo de Issues
    // Combinamos el resultado de la detección con la lista de heurísticas disponibles en la BD.
    val allIssues: StateFlow<Map<String, List<IssueWithBarriers>>> = combine(
        detectionResult,
        heuristicsRepository.getAll() //
    ) { result, heuristicsList ->
        // Si no hay detección o no hay heurísticas cargadas, devolvemos mapa vacío
        if (result == null || heuristicsList.isEmpty()) {
            emptyMap()
        } else {
            // Obtenemos las cajas de la detección actual
            val boxes = result.boundingBoxes

            // Para cada heurística, calculamos los issues pasando las cajas
            heuristicsList.associate { heuristic ->
                // Aquí ocurre la magia: la heurística procesa las cajas y devuelve issues
                val calculatedIssues = heuristic.calcular(boxes)

                // Mapeamos: Nombre de Heurística -> Lista de Issues calculados
                heuristic.name to calculatedIssues
            }.filterValues { it.isNotEmpty() } // (Opcional) Limpiamos las que no detectaron nada
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // 4. Filtrado para la UI (Issues totales MENOS los descartados)
    val issues: StateFlow<Map<String, List<IssueWithBarriers>>> = combine(allIssues, dismissedIssues) { all, dismissed ->
        all.mapValues { (_, heuristicIssues) ->
            heuristicIssues.filter { !dismissed.contains(it) }
        }.filterValues { it.isNotEmpty() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // 5. Estado de heurísticas descartadas (cuando se descartaron todos sus issues)
    val dismissedHeuristics: StateFlow<Set<String>> = combine(allIssues, dismissedIssues) { all, dismissed ->
        all.filter { (_, heuristicIssues) ->
            heuristicIssues.isNotEmpty() && heuristicIssues.all { dismissed.contains(it) }
        }.keys
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- Acciones de la UI ---

    fun dismissHeuristic(heuristicName: String) {
        val issuesToDismiss = allIssues.value[heuristicName] ?: return
        _dismissedIssues.update { current -> current + issuesToDismiss }
    }

    fun restoreHeuristic(heuristicName: String) {
        val issuesToRestore = allIssues.value[heuristicName] ?: return
        _dismissedIssues.update { current -> current - issuesToRestore.toSet() }
    }

    // --- Generación del Reporte ---

    suspend fun generateReport(reportName: String): Long? {
        val currentCategoryId = captureRepository.categoryId.value
        val currentImageUri = captureRepository.imageUri.value

        // Obtenemos los issues que quedaron activos (ya calculados y filtrados)
        val activeIssues = issues.value.values.flatten()

        return withContext(Dispatchers.IO) {
            try {
                val imageBytes = uriToByteArray(currentImageUri)

                val metadata = ReportMetadata(
                    name = reportName,
                    image = imageBytes,
                    categoryId = currentCategoryId
                )

                val reportEntity = ReportEntity(
                    metadata = metadata,
                    issues = activeIssues
                )

                reportRepository.insertReport(reportEntity)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun uriToByteArray(uri: Uri?): ByteArray {
        if (uri == null) return ByteArray(0)
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    override fun onCleared() {
        captureRepository.resetToOriginal()
        super.onCleared()
    }
}