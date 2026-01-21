package com.accesibilidad.accesibiliapp


import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import com.accesibilidad.accesibiliapp.vistas.report.list.ReportListScreen
import com.accesibilidad.accesibiliapp.vistas.settings.HeuristicListScreen


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.accesibilidad.accesibiliapp.vistas.camera.CameraDetectionScreen
import com.accesibilidad.accesibiliapp.vistas.camera.ImageDetectionContent
import com.accesibilidad.accesibiliapp.ui.theme.AccesibiliAppTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccesibiliAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Screen()
                }
            }
        }
    }
}
@Composable
fun Screen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "reports"
    ) {
        // Lista de reportes (Home)
        composable("reports") {
            ReportListScreen(navController = navController)
        }

        // Menú de heurísticas
        composable("heuristicMenu") {
            HeuristicListScreen(navController = navController)
        }

        // Detección de imagen desde URI (Argumento opcional)
        composable(
            route = "cameraDetection?imageUri={imageUri}",
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true // Importante para argumentos opcionales con '?'
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            ImageDetectionContent(
                navController = navController,
                imageUri = imageUri
            )
        }

        // Detección desde cámara
        composable("cameraDetection") {
            CameraDetectionScreen(navController = navController)
        }

        // Curación de bounding boxes
        composable("boundingBoxCuration") {
            //BoundingBoxCurationScreen(navController = navController)
        }

        // Revisión de heurísticas
        composable("heuristicReviewScreen") {
            //HeuristicReviewScreen(navController = navController)
        }

        // Detalles de reporte (Argumento obligatorio)
        composable(
            route = "reports/{reportId}",
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getLong("reportId") ?: -1L
            // Si tu Screen recibe el ID, pásalo aquí
            //ReportDetailsScreen(navController = navController /*, reportId = reportId */)
        }
    }
}