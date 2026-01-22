package com.accesibilidad.accesibiliapp.vistas.report.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.accesibilidad.accesibiliapp.data.entity.CategoryEntity
import com.accesibilidad.accesibiliapp.data.entity.ReportEntity
import com.accesibilidad.accesibiliapp.data.repository.CategoryRepository
import com.accesibilidad.accesibiliapp.data.repository.ReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: StateFlow<Long?> = savedStateHandle.getStateFlow("reportId", null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val report: StateFlow<ReportEntity?> = reportId
        .filterNotNull()
        .flatMapLatest { id ->
            reportRepository.getReport(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allCategories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateReportCategory(report: ReportEntity, newCategoryId: Long) {
        viewModelScope.launch {
            // Asumiendo que Report y sus metadatos son Data Classes con copy()
            val updatedReport = report.copy(
                metadata = report.metadata.copy(categoryId = newCategoryId)
            )
            reportRepository.updateReport(updatedReport)
        }
    }

    fun updateReportName(report: ReportEntity, newName: String) {
        viewModelScope.launch {
            val updatedReport = report.copy(
                metadata = report.metadata.copy(name = newName)
            )
            reportRepository.updateReport(updatedReport)
        }
    }

    fun deleteReport(report: ReportEntity) {
        viewModelScope.launch {
            reportRepository.deleteReport(report)
        }
    }
}