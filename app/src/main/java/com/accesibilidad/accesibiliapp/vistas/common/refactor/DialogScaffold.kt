package com.accesibilidad.accesibiliapp.vistas.common.refactor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DialogScaffold(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonText: String = "Guardar",
    showCancelButton: Boolean = false,
    cancelButtonText: String = "Cancelar",
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            // Si necesitas colores específicos, agrégalos aquí, si no usa los defaults de Material3
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Título
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contenido (inyectado)
                content()

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showCancelButton) {
                        TextButton(onClick = onDismiss) {
                            Text(text = cancelButtonText)
                        }
                    }
                    TextButton(onClick = onConfirm) {
                        Text(text = confirmButtonText)
                    }
                }
            }
        }
    }
}