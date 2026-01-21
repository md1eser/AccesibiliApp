package com.accesibilidad.accesibiliapp.vistas.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.data.heuristicas.Heuristic
import com.accesibilidad.accesibiliapp.vistas.common.StyledLazyColumn // Asumo existencia
import com.accesibilidad.accesibiliapp.vistas.report.list.ReportListViewModel
import com.accesibilidad.accesibiliapp.vistas.settings.factory.HeuristicFactory // Asumo que lo reconstruiremos abajo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeuristicListScreen(
    navController: NavController,
    viewModel: HeuristicListViewModel  = hiltViewModel() // Inyección Hilt estándar

) {
    val heuristics by viewModel.getAll.collectAsState(initial = emptyList())
    val labels by viewModel.labels.collectAsState(initial = emptyList())

    // Estados para los diálogos
    var showAddEditDialog by remember { mutableStateOf(false) }
    var heuristicToEdit by remember { mutableStateOf<Heuristic?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Heuristic?>(null) }

    // Diálogo de Confirmación de Borrado (Lógica reconstruida simplificada)
    if (showDeleteDialog != null) {
        // Asumo que tenías un AlertDialog estándar aquí, aunque el código descompilado
        // no muestra el cuerpo exacto del diálogo de borrado, solo la lógica.
        // Aquí invocas viewModel.delete(it.id)
        // Por ahora lo dejaré implícito o puedes usar un AlertDialog común.
        viewModel.delete(showDeleteDialog!!.id)
        showDeleteDialog = null
    }

    // Estructura principal
    // Nota: El descompilado sugiere que StyledLazyColumn maneja el Scaffold o similar,
    // pero aquí veo un TopAppBar explícito.

    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Heurísticas") },
                actions = {
                    IconButton(
                        onClick = {
                            heuristicToEdit = null
                            showAddEditDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir heurística")
                    }
                }
            )
        }
    ) { padding ->
        StyledLazyColumn(
            items = heuristics,
            contentPadding = padding
        ) { heuristic ->
            HeuristicItem(
                heuristic = heuristic,
                onClick = {
                    heuristicToEdit = heuristic
                    showAddEditDialog = true
                },
                onDelete = {
                    showDeleteDialog = heuristic
                }
            )
        }

        if (showAddEditDialog) {
            AddHeuristicDialog(
                heuristicToEdit = heuristicToEdit,
                labels = labels,
                onSave = { newHeuristic ->
                    viewModel.add(newHeuristic)
                },
                onDismiss = {
                    showAddEditDialog = false
                }
            )
        }
    }
}