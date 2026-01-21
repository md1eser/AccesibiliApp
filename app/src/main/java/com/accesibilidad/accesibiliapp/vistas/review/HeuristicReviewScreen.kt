package com.accesibilidad.accesibiliapp.vistas.review

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch // Importante para lanzar la corrutina

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeuristicReviewScreen(
    navController: NavController,
    viewModel: HeuristicReviewViewModel = hiltViewModel()
) {
    // 1. Necesitamos un Scope para lanzar acciones asíncronas desde botones (UI Events)
    val scope = rememberCoroutineScope()

    // Recolectar estados del ViewModel
    val allIssues by viewModel.allIssues.collectAsState()
    val issuesByHeuristic by viewModel.issues.collectAsState()
    val dismissedHeuristics by viewModel.dismissedHeuristics.collectAsState()

    // Estados locales de la UI
    var showNameReportDialog by remember { mutableStateOf(false) }
    var reportNameText by remember { mutableStateOf("") }

    val totalIssues = issuesByHeuristic.values.sumOf { it.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Problemáticas ($totalIssues)") },
                actions = {
                    TextButton(onClick = { showNameReportDialog = true }) {
                        Text("Continuar")
                    }
                }
            )
        }
    ) { paddingValues ->
        HeuristicList(
            modifier = Modifier.padding(paddingValues),
            allIssues = allIssues,
            issuesByHeuristic = issuesByHeuristic,
            dismissedHeuristics = dismissedHeuristics,
            onHeuristicClick = { heuristicName ->
                // Aquí iría tu lógica original de ver detalles si la necesitas
            },
            onToggleHeuristic = { heuristicName, isChecked ->
                if (isChecked) {
                    viewModel.restoreHeuristic(heuristicName)
                } else {
                    viewModel.dismissHeuristic(heuristicName)
                }
            }
        )
    }

    // --- Lógica del Diálogo y Guardado ---
    if (showNameReportDialog) {
        AlertDialog(
            onDismissRequest = { showNameReportDialog = false },
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
                Button(onClick = {
                    // 2. Lanzamos la corrutina aquí
                    scope.launch {
                        // Llamamos a la función suspendida del ViewModel
                        val reportId = viewModel.generateReport(reportNameText)

                        if (reportId != null) {
                            showNameReportDialog = false
                            // 3. Navegamos al detalle del reporte usando el ID generado
                            // El 'popUpTo' evita que el usuario vuelva a esta pantalla con "Atrás"
                            navController.navigate("reports/$reportId") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameReportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}