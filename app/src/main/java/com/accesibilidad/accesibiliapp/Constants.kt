package com.accesibilidad.accesibiliapp


/**
 * Contiene las rutas de los archivos de recursos para el modelo de TensorFlow Lite.
 */
object Constants {
    // Usamos 'const val' para que sean constantes de tiempo de compilación (más eficiente)
    const val MODEL_PATH = "best_float32.tflite"
    const val LABELS_PATH = "labels.txt"
}