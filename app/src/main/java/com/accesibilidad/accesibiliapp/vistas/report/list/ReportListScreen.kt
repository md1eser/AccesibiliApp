package com.accesibilidad.accesibiliapp.vistas.report.list


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.data.entity.CategoryEntity
import com.accesibilidad.accesibiliapp.vistas.common.refactor.StyledListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    navController: NavController,
    viewModel: ReportListViewModel = hiltViewModel() // Inyección Hilt estándar
) {
    // Estados recolectados del ViewModel
    val categories by viewModel.categoriesToShow.collectAsState()
    val categoryScores by viewModel.categoryScores.collectAsState()
    val reports by viewModel.reportsToShow.collectAsState()
    val currentCategory by viewModel.currentCategory.collectAsState()
    val canNavigateBack by viewModel.canNavigateBack.collectAsState()

    // Estados locales de la UI para diálogos
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<CategoryEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<CategoryEntity?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Variables temporales para inputs
    var text by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.prepareForCaptureWithImage(it)
            navController.navigate("cameraDetection")
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentCategory?.name ?: "") },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    } else {
                        IconButton(onClick = { navController.navigate("heuristicMenu") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ir a Configuraciones")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showImageSourceDialog = true }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Iniciar captura")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 8.dp)
        ) {
            // Lista de Categorías
            items(categories, key = { "category_${it.id}" }) { category ->
                val score = categoryScores[category.id]

                CategoryItem(
                    category = category,
                    score = score,
                    onClick = { viewModel.navigateToCategory(category.id) },
                    onRename = { showRenameDialog = it },
                    onDelete = { showDeleteDialog = it }
                )
            }

            // Lista de Reportes
            items(reports, key = { "report_${it?.metadata?.id}" }) { report ->
                StyledListItem(
                    onClick = { navController.navigate("reports/${report?.metadata?.id}") }
                ) {
                    Text(text = "${report?.metadata?.name}")
                }
            }

            // Botón para añadir nueva categoría al final
            item("add_category_item") {
                AddCategoryItem(onClick = { showAddCategoryDialog = true })
            }
        }
    }

    // Lógica de Diálogos (Simplificada a lo que se ve en el código)

    if (showAddCategoryDialog) {
        // Asumiendo un AlertDialog estándar o componente personalizado
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
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
                    viewModel.addCategory(text)
                    showAddCategoryDialog = false
                    text = "" // Reset
                }) { Text("Aceptar") }
            }
        )
    }

    showRenameDialog?.let { category ->
        LaunchedEffect(category) {
            newName = category.name
        }

        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
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
                    viewModel.updateCategoryName(category, newName)
                    showRenameDialog = null
                    newName = ""
                }) { Text("Renombrar") }
            }
        )
    }

    showDeleteDialog?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar categoría") },
            text = { Text("¿Estás seguro de eliminar ${category.name}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteCategory(category)
                    showDeleteDialog = null
                }) { Text("Eliminar") }
            }
        )
    }

    // Diálogo de fuente de imagen (Cámara o Galería)
    if (showImageSourceDialog) {
        // Implementación lógica inferida
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar fuente") },
            confirmButton = {
                Button(onClick = {
                    showImageSourceDialog = false
                    viewModel.prepareForCapture()
                    navController.navigate("cameraDetection")
                }) { Text("Cámara") }
            },
            dismissButton = {
                Button(onClick = {
                    showImageSourceDialog = false
                    imagePickerLauncher.launch("image/*")
                }) { Text("Galería") }
            }
        )
    }
}