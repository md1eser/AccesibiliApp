package com.accesibilidad.accesibiliapp.vistas.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox
import com.accesibilidad.accesibiliapp.vistas.common.refactor.Overlay

@Composable
fun ImageOverlayDialog(
    imageBitmap: ImageBitmap,
    boundingBoxes: List<BoundingBox>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    overlayColor: Color = Color.Red
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Black)
                .aspectRatio(
                    ratio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null, // decorative image
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )

            // Reusing your existing Overlay component
            Overlay(
                boundingBoxes = boundingBoxes,
                width = imageBitmap.width,
                height = imageBitmap.height,
                color = overlayColor
            )
        }
    }
}