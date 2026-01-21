package com.accesibilidad.accesibiliapp.data.deteccion

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext


import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate


import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
//import org.tensorflow.lite.support.image.



import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class Detector(
    private val context: Context,
    private val modelPath: String,
    private val labelPath: String?
) {

    companion object {
        private const val TAG = "Detector"
        private const val CONFIDENCE_THRESHOLD = 0.8f
        private const val IOU_THRESHOLD = 0.7f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
    }

    private val _results = MutableSharedFlow<DetectionResult>(extraBufferCapacity = 1)
    val results: SharedFlow<DetectionResult> = _results.asSharedFlow()

    private val _labels = MutableStateFlow<List<String>>(emptyList())
    val labels = _labels.asStateFlow()

    private var interpreter: Interpreter? = null
    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    private val options = Interpreter.Options()
    private val imageProcessor: ImageProcessor

    init {
        val compatibilityList = CompatibilityList()
        if (compatibilityList.isDelegateSupportedOnThisDevice) {
            options.addDelegate(GpuDelegate())
            Log.d(TAG, "GPU delegate activated.")
        } else {
            options.setNumThreads(4)
            Log.d(TAG, "GPU not supported. Using CPU.")
        }

        imageProcessor = ImageProcessor.Builder()
            .add(NormalizeOp(0.0f, 255.0f))
            .add(CastOp(INPUT_IMAGE_TYPE))
            .build()
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, options)

        var loadedLabels = MetaData.extractNamesFromMetadata(model)
        if (loadedLabels.isEmpty()) {
            if (labelPath != null) {
                loadedLabels = MetaData.extractNamesFromLabelFile(context, labelPath)
            } else {
                Log.d(TAG, "Model not contains metadata, provide LABELS_PATH in Constants.kt")
                loadedLabels = MetaData.TEMP_CLASSES
            }
        }
        _labels.emit(loadedLabels)

        val inputShape = interpreter?.getInputTensor(0)?.shape()
        val outputShape = interpreter?.getOutputTensor(0)?.shape()

        if (inputShape != null) {
            // Asumiendo formato [batch, width, height, channels] o [batch, channels, width, height]
            // El original tenía una lógica específica para inputShape[1] == 3
            tensorWidth = inputShape[1]
            tensorHeight = inputShape[2]

            // Corrección basada en el decompilado original para formatos transpuestos
            if (inputShape[1] == 3) {
                tensorWidth = inputShape[2]
                tensorHeight = inputShape[3]
            }
        }

        if (outputShape != null) {
            numChannel = outputShape[1]
            numElements = outputShape[2]
        }
    }

    suspend fun restart(isGpu: Boolean) = withContext(Dispatchers.IO) {
        interpreter?.close()
        // Nota: El parámetro isGpu no se usaba en el bloque original decompilado,
        // pero reinicializaba el interpreter.
        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, options)
    }

    fun isActive(): Boolean = interpreter != null

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    suspend fun detect(frame: Bitmap) {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter is not initialized.")
            return
        }
        if (tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) {
            Log.e(TAG, "Tensor dimensions are invalid. Detection aborted.")
            return
        }

        val inferenceStartTime = SystemClock.uptimeMillis()

        val resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false)
        val tensorImage = TensorImage(INPUT_IMAGE_TYPE)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer: ByteBuffer = processedImage.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, numElements), OUTPUT_IMAGE_TYPE)

        interpreter?.run(imageBuffer, output.buffer)

        val outputArray = output.floatArray
        val bestBoxes = bestBox(outputArray)

        val inferenceTime = SystemClock.uptimeMillis() - inferenceStartTime

        _results.emit(DetectionResult(bestBoxes ?: emptyList(), inferenceTime, frame))
    }

    private fun bestBox(array: FloatArray): List<BoundingBox>? {
        val boundingBoxes = ArrayList<BoundingBox>()
        val currentLabels = _labels.value

        // Iterar sobre los elementos detectados
        for (c in 0 until numElements) {
            var maxIdx = -1
            var maxConf = CONFIDENCE_THRESHOLD

            // Los primeros 4 valores son coordenadas, las clases empiezan en index 4
            // El array está aplanado, por eso saltamos de numElements en numElements

            // Buscar la clase con mayor confianza
            for (j in 4 until numChannel) {
                val arrayIdx = (numElements * j) + c
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                if (maxIdx in currentLabels.indices) {
                    val clsName = currentLabels[maxIdx]

                    // Extraer coordenadas
                    // c = cx, numElements+c = cy, etc...
                    val cx = array[c]
                    val cy = array[numElements + c]
                    val w = array[(numElements * 2) + c]
                    val h = array[(numElements * 3) + c]

                    val x1 = cx - (w / 2f)
                    val y1 = cy - (h / 2f)
                    val x2 = cx + (w / 2f)
                    val y2 = cy + (h / 2f)

                    if (x1 in 0f..1f && y1 in 0f..1f && x2 in 0f..1f && y2 in 0f..1f) {
                        boundingBoxes.add(
                            BoundingBox(x1, y1, x2, y2, cx, cy, w, h, maxConf, maxIdx, clsName)
                        )
                    }
                } else {
                    Log.e(TAG, "Invalid index for labels: $maxIdx")
                }
            }
        }

        if (boundingBoxes.isEmpty()) return null

        return applyNMS(boundingBoxes)
    }

    private fun applyNMS(boxes: List<BoundingBox>): List<BoundingBox> {
        val sortedBoxes = boxes.sortedByDescending { it.cnf }.toMutableList()
        val selectedBoxes = ArrayList<BoundingBox>()

        while (sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first()
            selectedBoxes.add(first)
            sortedBoxes.remove(first)

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }
        return selectedBoxes
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = max(box1.x1, box2.x1)
        val y1 = max(box1.y1, box2.y1)
        val x2 = min(box1.x2, box2.x2)
        val y2 = min(box1.y2, box2.y2)

        val intersectionArea = max(0f, x2 - x1) * max(0f, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h

        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }
}