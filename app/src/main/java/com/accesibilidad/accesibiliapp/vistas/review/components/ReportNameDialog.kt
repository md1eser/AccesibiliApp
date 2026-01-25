package com.accesibilidad.accesibiliapp.vistas.review.components

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
internal fun ReportNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // Local state for the text input
    var reportNameText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nombre del reporte") },
        text = {
            OutlinedTextField(
                value = reportNameText,
                onValueChange = { reportNameText = it },
                label = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reportNameText) },
                enabled = reportNameText.isNotBlank() // Good practice: disable if empty
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}