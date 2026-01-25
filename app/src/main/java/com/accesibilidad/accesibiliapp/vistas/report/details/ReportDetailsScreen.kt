package com.accesibilidad.accesibiliapp.vistas.report.details

import androidx.compose.ui.unit.IntSize // Por si lo querés usar
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import com.accesibilidad.accesibiliapp.vistas.common.ImageOverlayDialog

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
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Seleccionar categoría") },
            text = {
                // Usamos LazyColumn por si hay muchas categorías
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp) // Limita la altura para que no ocupe toda la pantalla
                ) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Llamamos al ViewModel para actualizar
                                    viewModel.updateReportCategory(report!!, category.id)
                                    showCategoryDialog = false
                                }
                                .padding(vertical = 8.dp), // Espaciado para mejor tacto
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Asumimos que report.metadata.categoryId es donde guardas el ID actual.
                            // Si el campo tiene otro nombre, ajusta esta línea.
                            val isSelected = report!!.metadata.categoryId == category.id

                            RadioButton(
                                selected = isSelected,
                                onClick = null // El click lo maneja la Row completa
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
        val imageByteArray = report?.metadata?.image

        // 1. Guardamos el tamaño real de la imagen escalada
        var imageSize by remember { mutableStateOf(IntSize.Zero) }

        val imageBitmap: ImageBitmap = remember(imageByteArray) {
            imageByteArray?.let {
                try {
                    BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap()
                } catch (e: Exception) { null }
            } as ImageBitmap
        }

        ImageOverlayDialog(
            imageBitmap = imageBitmap,
            // Transform your specific data into the generic list needed by the component
            boundingBoxes = selectedIssue!!.barriers.map { it.toBoundingBox() },
            onDismissRequest = { showIssueDetailDialog = false },
            overlayColor = Color.Red // Optional, defaults to Red
        )
    }




}