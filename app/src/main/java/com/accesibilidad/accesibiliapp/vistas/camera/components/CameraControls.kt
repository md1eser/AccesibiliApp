package com.accesibilidad.accesibiliapp.vistas.camera.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CameraControls(
    inferenceTime: Long,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onCaptureClick: () -> Unit
) {
    // Usamos un Box para superponer controles si fuera necesario,
    // pero aquí el Scaffold los organiza.
    // Este composable devuelve la UI que va DENTRO del Scaffold o sobre la cámara.

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Panel Superior (Info + Pausa)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter), // Lo pegamos arriba
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill de Inferencia
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.small,
                contentColor = Color.White
            ) {
                Text(
                    text = "Inferencia: ${inferenceTime}ms",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Botón Pausa (Mini)
            IconButton(
                onClick = onPauseToggle,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Reanudar" else "Pausar"
                )
            }
        }

        // 2. Botón Gigante de Captura (Abajo Centro)
        // Nota: En el Screen principal lo puse en el FAB del Scaffold,
        // pero también podrías ponerlo aquí si quieres control total de la posición.
        FloatingActionButton(
            onClick = onCaptureClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Capturar",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}