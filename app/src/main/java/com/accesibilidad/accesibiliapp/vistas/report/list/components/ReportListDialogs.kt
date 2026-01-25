package com.accesibilidad.accesibiliapp.vistas.report.list.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nombre") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(text)
                onDismiss()
            }) { Text("Aceptar") }
        }
    )
}

@Composable
internal fun RenameCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(categoryName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo nombre") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(newName)
                onDismiss()
            }) { Text("Renombrar") }
        }
    )
}

@Composable
internal fun DeleteCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar categoría") },
        text = { Text("¿Estás seguro de eliminar $categoryName?") },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) { Text("Eliminar") }
        }
    )
}