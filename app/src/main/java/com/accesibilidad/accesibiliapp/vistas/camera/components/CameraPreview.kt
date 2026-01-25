package com.accesibilidad.accesibiliapp.vistas.camera.components

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.accesibilidad.accesibiliapp.data.deteccion.DetectionResult
import com.accesibilidad.accesibiliapp.vistas.common.refactor.Overlay
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    detectionResult: DetectionResult?,
    isPaused: Boolean,
    onFrameCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Executor en hilo secundario para no bloquear la UI
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Vista nativa de Android para la cámara
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    // Control atómico para pausar el análisis (thread-safe)
    val isAnalysisPaused = remember { AtomicBoolean(isPaused) }

    // Sincronizamos el estado de pausa de Compose con el AtomicBoolean
    LaunchedEffect(isPaused) {
        isAnalysisPaused.set(isPaused)
    }

    // Limpieza de memoria al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Inicialización de CameraX
    LaunchedEffect(context, lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 1. Caso de uso: Vista Previa
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // 2. Caso de uso: Análisis de Imagen (IA)
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Solo procesar la última, descartar si va lento
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!isAnalysisPaused.get()) {
                            val bitmap = imageProxy.toBitmap()
                            if (bitmap != null) {
                                onFrameCaptured(bitmap)
                            }
                        }
                        imageProxy.close() // ¡IMPORTANTE! Siempre cerrar el proxy o la cámara se congela
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Fallo al vincular cámara", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = modifier) {
        // Capa 1: Video
        AndroidView(
            factory = { previewView },
            modifier = Modifier.matchParentSize()
        )

        // Capa 2: Dibujos (Bounding Boxes)
        Overlay(
            boundingBoxes = detectionResult?.boundingBoxes,
            width = detectionResult?.bitmap?.width ?: 0,
            height = detectionResult?.bitmap?.height ?: 0
        )
    }
}