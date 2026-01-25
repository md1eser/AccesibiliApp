package com.accesibilidad.accesibiliapp.vistas.camera

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.vistas.camera.components.CameraControls
import com.accesibilidad.accesibiliapp.vistas.camera.components.CameraPreview
import com.accesibilidad.accesibiliapp.vistas.camera.components.PermissionContent
import com.accesibilidad.accesibiliapp.vistas.common.refactor.LoadingScreen

@Composable
fun CameraDetectionScreen(
    navController: NavController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // 1. Estados del ViewModel
    val isProcessingExternal by viewModel.isProcessingExternalImage.collectAsState()
    val isDetectorInitialized by viewModel.isDetectorInitialized().collectAsState()
    val detectionResult by viewModel.getDetectionResult().collectAsState()
    val inferenceTime by viewModel.getInferenceTime().collectAsState()

    // 2. Estados Locales
    var isCameraPaused by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED
        )
    }

    // 3. Manejo de Permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        if (!isProcessingExternal) {
            viewModel.resetCaptureRepository()
        }
    }
    // 4. Manejo de Navegación (Eventos de una sola vez)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.getNavigationEvent().collect {
                navController.navigate("boundingBoxCuration") {
                    // Si venimos de galería externa, matamos la cámara del backstack para ahorrar memoria
                    if (isProcessingExternal) {
                        popUpTo("cameraDetection") { inclusive = true }
                    }
                }
            }
        }
    }

    // 5. El Árbol de Decisión de la UI (State Switching)
    if (!hasCameraPermission) {
        PermissionContent(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
        )
    } else if (isProcessingExternal) {
        LoadingScreen(text = "Procesando imagen externa...")
    } else if (!isDetectorInitialized) {
        LoadingScreen(text = "Iniciando motor IA...")
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // Quitamos el FAB del Scaffold porque CameraControls ya maneja su propia UI superpuesta
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                // 1. Capa de Video
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    detectionResult = detectionResult,
                    isPaused = isCameraPaused,
                    onFrameCaptured = { bitmap ->
                        viewModel.processFrame(
                            viewModel.rotateBitmap(bitmap, 90f),
                            false
                        )
                    }
                )

                // 2. Capa de Controles (Overlay UI)
                CameraControls(
                    inferenceTime = inferenceTime,
                    isPaused = isCameraPaused,
                    onPauseToggle = { isCameraPaused = !isCameraPaused },
                    onCaptureClick = { viewModel.onCaptureRequest() }
                )
            }
        }
    }

}