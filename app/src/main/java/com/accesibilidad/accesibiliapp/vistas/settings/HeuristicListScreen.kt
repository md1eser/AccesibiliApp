package com.accesibilidad.accesibiliapp.vistas.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.data.heuristicas.Heuristic
import com.accesibilidad.accesibiliapp.vistas.common.StyledLazyColumn
import com.accesibilidad.accesibiliapp.vistas.settings.components.*

@Composable
fun HeuristicListScreen(
    navController: NavController,
    viewModel: HeuristicListViewModel = hiltViewModel()
) {
    // 1. Recolección de Estado
    val heuristics by viewModel.getAll.collectAsState(initial = emptyList())
    val labels by viewModel.labels.collectAsState(initial = emptyList())

    // 2. Estados Locales (Gestión de Diálogos)
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Heuristic?>(null) }
    var heuristicToEdit by remember { mutableStateOf<Heuristic?>(null) }

    Scaffold(
        topBar = {
            HeuristicTopBar(
                onAddClick = {
                    heuristicToEdit = null // Modo Crear
                    showAddEditDialog = true
                }
            )
        }
    ) { padding ->

        // 3. La Lista
        StyledLazyColumn(
            items = heuristics,
            contentPadding = padding
        ) { heuristic ->
            HeuristicItem(
                heuristic = heuristic,
                onClick = {
                    heuristicToEdit = heuristic // Modo Editar
                    showAddEditDialog = true
                },
                onDelete = { showDeleteDialog = heuristic }
            )
        }
    }

    // 4. Gestión de Diálogos (Externalizados)

    if (showAddEditDialog) {
        AddEditHeuristicDialog(
            heuristicToEdit = heuristicToEdit,
            labels = labels,
            onDismiss = { showAddEditDialog = false },
            onSave = { heuristic ->
                viewModel.add(heuristic) // El repositorio maneja si es update o insert por el ID
            }
        )
    }

    showDeleteDialog?.let { heuristic ->
        DeleteHeuristicDialog(
            heuristicName = heuristic.name,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.delete(heuristic.id)
            }
        )
    }
}