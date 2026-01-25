package com.accesibilidad.accesibiliapp.vistas.review

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.vistas.common.ImageOverlayDialog
import com.accesibilidad.accesibiliapp.vistas.review.components.ReportNameDialog
import com.accesibilidad.accesibiliapp.vistas.review.components.ReviewTopBar
import com.accesibilidad.accesibiliapp.vistas.review.components.HeuristicList
import kotlinx.coroutines.launch

@Composable
fun HeuristicReviewScreen(
    navController: NavController,
    viewModel: HeuristicReviewViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    // 1. State Collection
    val allIssues by viewModel.allIssues.collectAsState()
    val issuesByHeuristic by viewModel.issues.collectAsState()
    val dismissedHeuristics by viewModel.dismissedHeuristics.collectAsState()

    // 2. Local UI State
    var showNameReportDialog by remember { mutableStateOf(false) }
    var showImageOverlayDialog by remember { mutableStateOf(false) }
    var selectedHeuristicName by remember { mutableStateOf("") }

    // 3. Derived State
    val totalIssues = remember(issuesByHeuristic) { issuesByHeuristic.values.sumOf { it.size } }
    val image = viewModel.getImage()?.asImageBitmap()

    Scaffold(
        topBar = {
            ReviewTopBar(
                issueCount = totalIssues,
                onContinueClick = { showNameReportDialog = true }
            )
        }
    ) { paddingValues ->

        HeuristicList(
            modifier = Modifier.padding(paddingValues),
            allIssues = allIssues,
            issuesByHeuristic = issuesByHeuristic,
            dismissedHeuristics = dismissedHeuristics,
            onHeuristicClick = { heuristicName ->
                selectedHeuristicName = heuristicName
                showImageOverlayDialog = true
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

    // --- Dialogs ---

    if (showNameReportDialog) {
        ReportNameDialog(
            onDismiss = { showNameReportDialog = false },
            onConfirm = { reportName ->
                // Keep the async logic here in the Screen, the Dialog shouldn't know about Coroutines
                scope.launch {
                    val reportId = viewModel.generateReport(reportName)
                    if (reportId != null) {
                        showNameReportDialog = false
                        navController.navigate("reports/$reportId") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        )
    }

    // Logic for the Overlay is now cleaner
    if (showImageOverlayDialog && image != null) {
        // Calculate boxes only when dialog is shown
        val boundingBoxes = remember(selectedHeuristicName, issuesByHeuristic) {
            issuesByHeuristic[selectedHeuristicName]
                ?.flatMap { it.barriers }
                ?.map { it.toBoundingBox() }
                ?: emptyList()
        }

        ImageOverlayDialog(
            imageBitmap = image,
            boundingBoxes = boundingBoxes,
            onDismissRequest = { showImageOverlayDialog = false },
            overlayColor = Color.Red
        )
    }
}