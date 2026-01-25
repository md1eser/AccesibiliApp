package com.accesibilidad.accesibiliapp.vistas.report.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // Bordes redondeados pero no exagerados, como en tu captura
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Seleccionar Imagen",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Normal, // En la imagen no parece bold
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Opción Cámara
                SourceOptionItem(
                    icon = Icons.Default.PhotoCamera,
                    label = "Capturar en tiempo real",
                    onClick = {
                        onCameraClick()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Opción Galería
                SourceOptionItem(
                    icon = Icons.Default.Image,
                    label = "Buscar en galería",
                    onClick = {
                        onGalleryClick()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {} // Se deja vacío para que no aparezcan botones extra abajo
    )
}

@Composable
private fun SourceOptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp), // Tamaño similar al de tu imagen
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}