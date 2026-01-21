package com.accesibilidad.accesibiliapp.vistas.camera

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.accesibilidad.accesibiliapp.vistas.common.refactor.LoadingScreen

@Composable
fun CameraDetectionScreen(
    navController: NavController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    CameraDetectionContent(
        navController = navController,
        viewModel = viewModel
    )
}

@Composable
private fun CameraDetectionContent(
    navController: NavController,
    viewModel: CameraViewModel
) {
    val context = LocalContext.current

    // Estado para controlar si el permiso está concedido
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PermissionChecker.PERMISSION_GRANTED
        )
    }

    // Launcher para solicitar el permiso
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Estado de inicialización del detector
    val isDetectorInitialized by viewModel.isDetectorInitialized().collectAsState()

    // Solicitar permiso automáticamente si no está concedido
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    when {
        hasCameraPermission -> {
            if (isDetectorInitialized) {
                // Si hay permiso y el detector está listo, mostramos la cámara
                CameraWithDetection(
                    navController = navController,
                    viewModel = viewModel
                )
            } else {
                // Pantalla de carga mientras se inicializa TensorFlow/Modelo
                LoadingScreen(text = "Cargando el detector...")
            }
        }
        else -> {
            // UI para solicitar permiso manualmente si fue denegado
            PermissionRequestUI(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }
    }
}

@Composable
private fun CameraWithDetection(
    navController: NavController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    // 1. Colección de estados del ViewModel
    val detectionResult by viewModel.getDetectionResult().collectAsState()
    val inferenceTime by viewModel.getInferenceTime().collectAsState()

    // 2. Estado local de la UI
    var isCameraPaused by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // 3. Efectos (Side-Effects)

    // Resetear el repositorio al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.resetCaptureRepository()
    }

    // Escuchar evento de navegación hacia "boundingBoxCuration"
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.getNavigationEvent().collect {
                navController.navigate("boundingBoxCuration")
            }
        }
    }

    // 4. Interfaz de Usuario
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            // Botón de Captura
            FloatingActionButton(
                onClick = {
                    viewModel.onCaptureRequest()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Capturar"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Componente de Cámara y Overlay (CameraOutput)
            CameraOutput(
                detection = detectionResult,
                modifier = Modifier.fillMaxSize(),
                isPaused = isCameraPaused,
                onFrame = { frame ->
                    if (!isCameraPaused) {
                        Log.d("CameraDetectionScreen", "Frame received")
                        // Rotamos 90 grados porque la cámara suele venir en Landscape
                        viewModel.processFrame(
                            bitmap = viewModel.rotateBitmap(frame, 90f),
                            triggerNavigation = false
                        )
                    }
                }
            )

            // Panel de información superior (Inferencia y Control de Pausa)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Texto de tiempo de inferencia
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Inferencia: ${inferenceTime}ms",
                        color = Color.White,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Botón para Pausar/Reanudar análisis
                IconButton(
                    onClick = { isCameraPaused = !isCameraPaused },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isCameraPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isCameraPaused) "Reanudar" else "Pausar"
                    )
                }
            }
        }
    }
}