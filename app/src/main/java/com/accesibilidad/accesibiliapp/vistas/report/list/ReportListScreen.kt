package com.accesibilidad.accesibiliapp.vistas.report.list

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.data.entity.CategoryEntity
import com.accesibilidad.accesibiliapp.vistas.common.refactor.StyledListItem
import com.accesibilidad.accesibiliapp.vistas.report.list.components.*

@Composable
fun ReportListScreen(
    navController: NavController,
    viewModel: ReportListViewModel = hiltViewModel()
) {
    // 1. State Collection (The Data)
    val categories by viewModel.categoriesToShow.collectAsState()
    val categoryScores by viewModel.categoryScores.collectAsState()
    val reports by viewModel.reportsToShow.collectAsState()
    val currentCategory by viewModel.currentCategory.collectAsState()
    val canNavigateBack by viewModel.canNavigateBack.collectAsState()

    // 2. Local UI State (The Visibility Flags)
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<CategoryEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<CategoryEntity?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // 3. Side Effects & Launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.prepareForCaptureWithImage(it)
            navController.navigate("cameraDetection")
        }
    }

    // 4. The UI Structure
    Scaffold(
        topBar = {
            ReportListTopBar(
                title = currentCategory?.name ?: "",
                canNavigateBack = canNavigateBack,
                onBackClick = { viewModel.navigateBack() },
                onSettingsClick = { navController.navigate("heuristicMenu") },
                onCameraClick = { showImageSourceDialog = true }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            // Categories
            items(categories, key = { "category_${it.id}" }) { category ->
                CategoryItem( // Assuming this exists or is imported
                    category = category,
                    score = categoryScores[category.id],
                    onClick = { viewModel.navigateToCategory(category.id) },
                    onRename = { showRenameDialog = it },
                    onDelete = { showDeleteDialog = it }
                )
            }

            // Reports
            items(reports, key = { "report_${it?.metadata?.id}" }) { report ->
                StyledListItem(
                    onClick = { navController.navigate("reports/${report?.metadata?.id}") }
                ) {
                    Text(text = "${report?.metadata?.name}")
                }
            }

            // Add Button
            item("add_category_item") {
                AddCategoryItem(onClick = { showAddCategoryDialog = true })
            }
        }
    }

    // 5. Dialogs Management (Moved logic out)
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name -> viewModel.addCategory(name) }
        )
    }

    showRenameDialog?.let { category ->
        RenameCategoryDialog(
            categoryName = category.name,
            onDismiss = { showRenameDialog = null },
            onConfirm = { newName -> viewModel.updateCategoryName(category, newName) }
        )
    }

    showDeleteDialog?.let { category ->
        DeleteCategoryDialog(
            categoryName = category.name,
            onDismiss = { showDeleteDialog = null },
            onConfirm = { viewModel.deleteCategory(category) }
        )
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraClick = {
                showImageSourceDialog = false
                viewModel.prepareForCapture()
                navController.navigate("cameraDetection")
            },
            onGalleryClick = {
                showImageSourceDialog = false
                imagePickerLauncher.launch("image/*")
            }
        )
    }
}