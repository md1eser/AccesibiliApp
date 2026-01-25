package com.accesibilidad.accesibiliapp.vistas.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeuristicTopBar(
    onAddClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Heurísticas") },
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir heurística")
            }
        }
    )
}