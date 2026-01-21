package com.accesibilidad.accesibiliapp.vistas.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

// Paleta de colores extraída de la lista estática
private val colorPalette = listOf(
    Color(0xFFFF6B36),
    Color(0xFFF44663),
    Color(0xFFA75530),
    Color(0xFF726537),
    Color(0xFF4A7D35),
    Color(0xFF2C9333),
    Color(0xFF0EA374),
    Color(0xFF0B5794),
    Color(0xFF0B3148),
    Color(0xFF573650),
    Color(0xFF96624A),
    Color(0xFFD89279),
    Color(0xFFFFF13B),
    Color(0xFFFFC707),
    Color(0xFFFF9E00),
    Color(0xFFFF5D22),
    Color(0xFF849F08),
    Color(0xFFA9E8DE),
    Color(0xFF6B6E0B)
)

@Composable
fun rememberColorForId(id: Any): Color {
    return remember(id) {
        generateColorForId(id)
    }
}

fun generateColorForId(id: Any): Color {
    val index = id.hashCode().absoluteValue % colorPalette.size
    return colorPalette[index]
}