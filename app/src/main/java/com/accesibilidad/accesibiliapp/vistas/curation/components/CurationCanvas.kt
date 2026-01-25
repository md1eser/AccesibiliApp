package com.accesibilidad.accesibiliapp.vistas.curation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.accesibilidad.accesibiliapp.vistas.common.refactor.Overlay
import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox // Assuming this import

@Composable
internal fun CurationCanvas(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    activeBoxes: List<BoundingBox>,
    inactiveBoxes: List<BoundingBox>,
    onBoxToggle: (BoundingBox) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Background Image
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Imagen a curar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // 2. Active Layer (Red)
        if (activeBoxes.isNotEmpty()) {
            Overlay(
                boundingBoxes = activeBoxes,
                width = bitmap.width,
                height = bitmap.height,
                color = Color.Red,
                onBoxClick = onBoxToggle
            )
        }

        // 3. Inactive Layer (Ghosted/Gray)
        if (inactiveBoxes.isNotEmpty()) {
            Overlay(
                boundingBoxes = inactiveBoxes,
                width = bitmap.width,
                height = bitmap.height,
                color = Color.Gray.copy(alpha = 0.8f),
                onBoxClick = onBoxToggle
            )
        }
    }
}