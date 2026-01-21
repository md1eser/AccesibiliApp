package com.accesibilidad.accesibiliapp.vistas.curation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.vistas.common.refactor.Overlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoundingBoxCurationScreen(
    navController: NavController,
    viewModel: BoundingBoxCurationViewModel = hiltViewModel()
) {
    // Recolectamos los estados del ViewModel
    val originalResult by viewModel.originalDetectionResult.collectAsState()
    val inactiveBoxes by viewModel.inactiveBoxes.collectAsState()

    // TopAppBar es experimental en M3, por eso la anotación arriba
    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") }, // El título estaba vacío en el original
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.confirmCuration()
                            navController.navigate("heuristicReviewScreen")
                        }
                    ) {
                        Text("Continuar")
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            originalResult?.let { result ->
                // 1. Dibujar la imagen de fondo
                Image(
                    bitmap = result.bitmap.asImageBitmap(),
                    contentDescription = "Imagen a curar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                val width = result.bitmap.width
                val height = result.bitmap.height

                // Separamos las cajas activas de las inactivas para pintarlas diferente
                val activeBoxes = result.boundingBoxes.filter { !inactiveBoxes.contains(it) }
                val inactiveBoxesList = inactiveBoxes.toList()

                // 2. Dibujar cajas activas (Rojo)
                Overlay(
                    boundingBoxes = activeBoxes,
                    width = width,
                    height = height,
                    color = Color.Red,
                    onBoxClick = { tappedBox ->
                        viewModel.toggleBoundingBox(tappedBox)
                    }
                )

                // 3. Dibujar cajas inactivas (Grisáceo/Transparente)
                // El decompilado mostraba un color gris con alfa modificado
                val inactiveColor = Color.Gray.copy(alpha = 0.8f)

                Overlay(
                    boundingBoxes = inactiveBoxesList,
                    width = width,
                    height = height,
                    color = inactiveColor,
                    onBoxClick = { tappedBox ->
                        viewModel.toggleBoundingBox(tappedBox)
                    }
                )
            }
        }
    }
}