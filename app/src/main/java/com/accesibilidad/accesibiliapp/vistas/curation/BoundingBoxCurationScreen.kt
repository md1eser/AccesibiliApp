package com.accesibilidad.accesibiliapp.vistas.curation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.vistas.curation.components.CurationCanvas
import com.accesibilidad.accesibiliapp.vistas.curation.components.CurationTopBar

@Composable
fun BoundingBoxCurationScreen(
    navController: NavController,
    viewModel: BoundingBoxCurationViewModel = hiltViewModel()
) {
    // 1. State Collection
    val originalResult by viewModel.originalDetectionResult.collectAsState()
    val inactiveBoxes by viewModel.inactiveBoxes.collectAsState()

    // 2. Derived State (Optimization)
    // We calculate the active boxes only when data changes, not on every redraw
    val activeBoxes = remember(originalResult, inactiveBoxes) {
        originalResult?.boundingBoxes?.filter { !inactiveBoxes.contains(it) } ?: emptyList()
    }

    val currentInactiveList = remember(inactiveBoxes) {
        inactiveBoxes.toList()
    }

    Scaffold(
        topBar = {
            CurationTopBar(
                onContinueClick = {
                    viewModel.confirmCuration()
                    navController.navigate("heuristicReviewScreen")
                }
            )
        }
    ) { paddingValues ->

        // 3. The UI Component
        // We only render the canvas if we have a result
        originalResult?.let { result ->
            CurationCanvas(
                modifier = Modifier.padding(paddingValues),
                bitmap = result.bitmap,
                activeBoxes = activeBoxes,
                inactiveBoxes = currentInactiveList,
                onBoxToggle = { box -> viewModel.toggleBoundingBox(box) }
            )
        }
    }
}