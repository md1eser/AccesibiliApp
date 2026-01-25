package com.accesibilidad.accesibiliapp.vistas.report.details.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailsTopBar(
    title: String,
    onBackClick: () -> Unit,
    onTitleClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.clickable { onTitleClick() }
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = onCategoryClick) {
                Icon(Icons.Default.Category, contentDescription = "Cambiar categoría")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar reporte")
            }
        }
    )
}