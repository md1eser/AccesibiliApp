package com.accesibilidad.accesibiliapp.vistas.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun ImageDetectionContent(
    navController: NavController,
    imageUri: String?,
    viewModel: CameraViewModel  = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Navegación cuando se completa la detección
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.getNavigationEvent().collect {
                // Navegar y limpiar el backstack para que volver atrás no re-procese
                navController.navigate("boundingBoxCuration") {
                    popUpTo("reports") { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }

    // Procesamiento de la imagen
    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            // Esperar a que el detector esté listo
            viewModel.isDetectorInitialized().filter { it }.first()

            val uri = Uri.parse(imageUri)
            val bitmap = decodeBitmapFromUri(context, uri)

            // Procesar con triggerNavigation = true para ir directo a la edición
            viewModel.processFrame(bitmap, triggerNavigation = true)
        }
    }
}

fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap {
    context.contentResolver.openInputStream(uri).use { inputStream ->
        return BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("No se pudo decodificar el Bitmap")
    }
}