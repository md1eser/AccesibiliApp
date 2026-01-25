package com.accesibilidad.accesibiliapp.vistas.report.details

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import com.accesibilidad.accesibiliapp.vistas.common.ImageOverlayDialog
import com.accesibilidad.accesibiliapp.vistas.report.details.components.*

@Composable
fun ReportDetailsScreen(
    navController: NavController,
    viewModel: ReportDetailsViewModel = hiltViewModel()
) {
    // 1. Estados Globales
    val report by viewModel.report.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    // 2. Estados Locales (Visibilidad de diálogos)
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Estado para el detalle (Overlay)
    var selectedIssueForOverlay by remember { mutableStateOf<IssueWithBarriers?>(null) }

    // 3. Loading State
    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentReport = report!! // Smart cast seguro aquí abajo

    Scaffold(
        topBar = {
            ReportDetailsTopBar(
                title = currentReport.metadata.name,
                onBackClick = { navController.popBackStack() },
                onTitleClick = { showRenameDialog = true },
                onCategoryClick = { showCategoryDialog = true },
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    ) { paddingValues ->

        // 4. Contenido Principal
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // Gráfico (40% pantalla)
            IssueScoreChart(
                issues = currentReport.issues,
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Text(
                text = "Problemáticas Encontradas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Lista (60% pantalla)
            IssueList(
                issues = currentReport.issues,
                onIssueClick = { issue ->
                    // Buscamos el objeto completo para el overlay
                    selectedIssueForOverlay = currentReport.issues.find { it.issue.id == issue.id }
                },
                modifier = Modifier.weight(0.6f)
            )
        }
    }

    // --- 5. Gestión de Diálogos (Externalizados) ---

    if (showRenameDialog) {
        RenameReportDialog(
            currentName = currentReport.metadata.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName -> viewModel.updateReportName(currentReport, newName) }
        )
    }

    if (showCategoryDialog) {
        ChangeCategoryDialog(
            categories = categories,
            currentCategoryId = currentReport.metadata.categoryId,
            onDismiss = { showCategoryDialog = false },
            onCategorySelected = { category -> viewModel.updateReportCategory(currentReport, category.id) }
        )
    }

    if (showDeleteDialog) {
        DeleteReportDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteReport(currentReport)
                navController.popBackStack()
            }
        )
    }

    // --- 6. Lógica de Overlay (Imagen) ---
    if (selectedIssueForOverlay != null) {
        val imageBytes = currentReport.metadata.image

        // Decodificación eficiente usando remember
        val imageBitmap = remember(imageBytes) {
            imageBytes?.let {
                try {
                    BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap()
                } catch (e: Exception) { null }
            }
        }

        if (imageBitmap != null) {
            ImageOverlayDialog(
                imageBitmap = imageBitmap,
                boundingBoxes = selectedIssueForOverlay!!.barriers.map { it.toBoundingBox() },
                onDismissRequest = { selectedIssueForOverlay = null },
                overlayColor = Color.Red
            )
        } else {
            // Fallback si no hay imagen (opcional: cerrar el diálogo o mostrar error)
            LaunchedEffect(Unit) { selectedIssueForOverlay = null }
        }
    }
}