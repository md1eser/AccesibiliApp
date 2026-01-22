package com.accesibilidad.accesibiliapp.vistas.report.details


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailsScreen(
    navController: NavController,
    viewModel: ReportDetailsViewModel  = hiltViewModel()
) {
    // Recolectar estados del ViewModel
    val report by viewModel.report.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    // Estados locales para diálogos
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showIssueDetailDialog by remember { mutableStateOf(false) }
    var selectedIssue by remember { mutableStateOf<IssueWithBarriers?>(null) }
    var newName by remember { mutableStateOf("") }

    // Si el reporte es nulo, mostrar carga
    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Contenido Principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = report!!.metadata.name,
                        modifier = Modifier.clickable {
                            newName = report!!.metadata.name
                            showRenameDialog = true
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(Icons.Default.Category, contentDescription = "Cambiar categoría")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar reporte")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            ReportDetailsContent(
                report = report!!,
                onIssueClick = { issue ->
                    // Buscar el IssueWithBarriers correspondiente al Issue simple
                    selectedIssue = report!!.issues.find { it.issue.id == issue.id }
                    showIssueDetailDialog = true
                }
            )
        }
    }

    // --- Diálogos ---

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Nuevo nombre") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateReportName(report!!, newName)
                    showRenameDialog = false
                }) {
                    Text("Guardar")
                }
            }
        )
    }

    if (showCategoryDialog) {
        // Aquí iría la implementación de tu diálogo de categorías
        // Probablemente un AlertDialog con una lista LazyColumn de 'categories'
        // Al seleccionar: viewModel.updateReportCategory(report!!, category.id)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar reporte?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteReport(report!!)
                    showDeleteDialog = false
                    navController.popBackStack()
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showIssueDetailDialog && selectedIssue != null) {
        // Asumiendo que IssueDetailScreen es un Dialog o una pantalla nueva
        // Según el código descompilado, parece que se pasa un Bitmap.
        // Nota: Asegúrate de tener el bitmap disponible o pasar null si es opcional.
        // IssueDetailScreen(issueWithBarriers = selectedIssue!!, reportBitmap = ...)
    }
}