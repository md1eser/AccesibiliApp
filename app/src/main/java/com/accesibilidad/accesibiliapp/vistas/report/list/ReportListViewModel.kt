package com.accesibilidad.accesibiliapp.vistas.report.list

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.accesibilidad.accesibiliapp.data.entity.CategoryEntity
import com.accesibilidad.accesibiliapp.data.entity.ReportEntity
import com.accesibilidad.accesibiliapp.data.repository.CaptureRepository
import com.accesibilidad.accesibiliapp.data.repository.CategoryRepository
import com.accesibilidad.accesibiliapp.data.repository.ReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportListViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val categoryRepository: CategoryRepository,
    private val captureRepository: CaptureRepository
) : ViewModel() {

    private val _currentParentId = MutableStateFlow<Long?>(null)
    val currentParentId: StateFlow<Long?> = _currentParentId.asStateFlow()

    // Lógica restaurada de flatMapLatest para cambiar entre categorías raíz y subcategorías
    @OptIn(ExperimentalCoroutinesApi::class)
    val categoriesToShow: StateFlow<List<CategoryEntity>> = _currentParentId.flatMapLatest { id ->
        if (id == null) {
            categoryRepository.getRootCategories()
        } else {
            categoryRepository.getSubcategories(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lógica compleja de combinación de flujos para obtener puntajes
    val categoryScores: StateFlow<Map<Long, Float>> = categoriesToShow.flatMapLatest { categories ->
        if (categories.isEmpty()) {
            flowOf(emptyMap())
        } else {
            // Mapea cada categoría a un Flow de su puntaje y luego los combina
            val flows = categories.map { category ->
                reportRepository.getAverageScoreForCategoryTree(category.id)
                    .map { score -> category.id to score }
            }
            combine(flows) { scores ->
                scores.toMap()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Filtra los reportes según la categoría actual
    val reportsToShow: StateFlow<List<ReportEntity?>> = combine(
        reportRepository.getAllReports(),
        currentParentId
    ) { allReports, parentId ->
        allReports.filter { it?.metadata?.categoryId == parentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Obtiene el objeto Category actual basado en el ID seleccionado
    val currentCategory: StateFlow<CategoryEntity?> = combine(
        categoryRepository.getAllCategories(),
        currentParentId
    ) { allCategories, parentId ->
        parentId?.let { id -> allCategories.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val canNavigateBack: StateFlow<Boolean> = _currentParentId.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val navigationStack = mutableListOf<Long?>()

    fun navigateToCategory(categoryId: Long) {
        navigationStack.add(_currentParentId.value)
        _currentParentId.value = categoryId
    }

    fun navigateBack() {
        if (navigationStack.isNotEmpty()) {
            _currentParentId.value = navigationStack.removeAt(navigationStack.lastIndex)
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            val newCategory = CategoryEntity(
                id = 0, // Asumiendo autoincrement
                name = name,
                parentId = _currentParentId.value,
                //type = 1
            )
            categoryRepository.insert(newCategory)
        }
    }

    fun updateCategoryName(category: CategoryEntity, newName: String) {
        viewModelScope.launch {
            val updatedCategory = category.copy(name = newName)
            categoryRepository.updateCategory(updatedCategory)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }

    fun prepareForCapture() {
        captureRepository.setCategoryId(currentParentId.value)
    }

    fun prepareForCaptureWithImage(imageUri: Uri) {
        captureRepository.setCategoryId(currentParentId.value)
        captureRepository.setImageUri(imageUri)
    }
}