package com.accesibilidad.accesibiliapp.vistas.common.refactor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox
import kotlin.math.min

@Composable
fun Overlay(
    boundingBoxes: List<BoundingBox>?,
    width: Int,  // ← Ancho de la imagen original
    height: Int, // ← Alto de la imagen original
    color: Color = Color.Red,
    onBoxClick: (BoundingBox) -> Unit = {}
) {
    if (boundingBoxes == null || width == 0 || height == 0) return

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current

        // Escalado
        val scale = min(
            constraints.maxWidth.toFloat() / width,
            constraints.maxHeight.toFloat() / height
        )

        // Offsets para centrado
        val offsetX = (constraints.maxWidth - (width * scale)) / 2
        val offsetY = (constraints.maxHeight - (height * scale)) / 2

        boundingBoxes.forEach { box ->
            BoundingBoxItem(
                box = box,
                width = width,   // ← AÑADE ESTO
                height = height, // ← AÑADE ESTO
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                color = color,
                onClick = { onBoxClick(box) }
            )
        }
    }
}

@Composable
private fun BoundingBoxItem(
    box: BoundingBox,
    width: Int,   // ← AÑADE ESTO
    height: Int,  // ← AÑADE ESTO
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    color: Color,
    onClick: () -> Unit
) {
    val density = LocalDensity.current

    // ✅ TRANSFORMACIÓN CORRECTA (igual que el compilado)
    val x1Hat = (box.x1 * width * scale) + offsetX
    val y1Hat = (box.y1 * height * scale) + offsetY
    val wHat = box.w * width * scale
    val hHat = box.h * height * scale

    // Convertir a Dp
    val xDp = with(density) { x1Hat.toDp() }
    val yDp = with(density) { y1Hat.toDp() }
    val wDp = with(density) { wHat.toDp() }
    val hDp = with(density) { hHat.toDp() }
    val textYDp = with(density) { (y1Hat - 35f).toDp() }

    Box {
        // Borde
        Box(
            modifier = Modifier
                .offset(x = xDp, y = yDp)
                .size(width = wDp, height = hDp)
                .border(width = 3.dp, color = color)
                .clickable { onClick() }
        )

        // Etiqueta
        Text(
            text = "${box.clsName} ${String.format("%.2f", box.cnf)}",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .offset(x = xDp, y = textYDp)
                .background(color)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}