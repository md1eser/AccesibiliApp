package com.accesibilidad.accesibiliapp.data.deteccion


import android.graphics.Bitmap

data class DetectionResult(
    val boundingBoxes: List<BoundingBox>,
    val inferenceTime: Long,
    val bitmap: Bitmap
) {
    fun deepCopy(): DetectionResult {
        return DetectionResult(
            boundingBoxes = boundingBoxes.map { it.copy() },
            inferenceTime = inferenceTime,
            bitmap = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        )
    }
}