package com.accesibilidad.accesibiliapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define tus colores personalizados
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color.White,           // ← Fondo blanco
    onBackground = Color.Black,         // ← Texto negro sobre fondo
    surface = Color.White,              // ← Superficies blancas
    onSurface = Color.Black,            // ← Texto negro sobre superficies
    error = Color(0xFFB00020),
    onError = Color.White
)

// Opcional: tema oscuro
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White
)

@Composable
fun AccesibiliAppTheme(
    darkTheme: Boolean = false, // Puedes forzar siempre claro con false
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Define tu tipografía si la tienes
        content = content
    )
}