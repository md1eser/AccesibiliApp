package com.accesibilidad.accesibiliapp.data.repository


import android.net.Uri
import com.accesibilidad.accesibiliapp.data.deteccion.DetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureRepository @Inject constructor() {

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId = _categoryId.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult = _detectionResult.asStateFlow()

    private val _originalDetectionResult = MutableStateFlow<DetectionResult?>(null)

    fun setCategoryId(id: Long?) { _categoryId.value = id }

    fun setImageUri(uri: Uri?) { _imageUri.value = uri }

    fun setDetectionResult(result: DetectionResult) {
        if (_originalDetectionResult.value == null) {
            _originalDetectionResult.value = result.deepCopy()
        }
        _detectionResult.value = result.deepCopy()
    }

    fun resetToOriginal() {
        _detectionResult.value = _originalDetectionResult.value?.deepCopy()
    }

    fun clear() {
        _categoryId.value = null
        _imageUri.value = null
        _detectionResult.value = null
        _originalDetectionResult.value = null
    }
}