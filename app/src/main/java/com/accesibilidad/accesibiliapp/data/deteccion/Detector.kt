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

    private val imageProcessor: ImageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(0f, 255f))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    // -----------------------------
    // Inicialización segura
    // -----------------------------
    suspend fun initialize() = withContext(Dispatchers.Default) {
        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = createInterpreter(model)

        var loadedLabels = MetaData.extractNamesFromMetadata(model)
        if (loadedLabels.isEmpty()) {
            loadedLabels = if (labelPath != null) {
                MetaData.extractNamesFromLabelFile(context, labelPath)
            } else {
                Log.w(TAG, "No labels found, using temp classes")
                MetaData.TEMP_CLASSES
            }
        }
        _labels.emit(loadedLabels)

        val inputShape = interpreter!!.getInputTensor(0).shape()
        val outputShape = interpreter!!.getOutputTensor(0).shape()

        // Input: [1, w, h, c] o [1, c, w, h]
        tensorWidth = inputShape[1]
        tensorHeight = inputShape[2]
        if (inputShape[1] == 3) {
            tensorWidth = inputShape[2]
            tensorHeight = inputShape[3]
        }

        // Output: [1, channels, elements]
        numChannel = outputShape[1]
        numElements = outputShape[2]
    }

    private fun createInterpreter(model: ByteBuffer): Interpreter {
        val compatibilityList = CompatibilityList()

        if (compatibilityList.isDelegateSupportedOnThisDevice) {
            try {
                Log.i(TAG, "Trying GPU delegate...")

                val gpuDelegate = GpuDelegate()
                val gpuOptions = Interpreter.Options().apply {
                    addDelegate(gpuDelegate)
                }

                val gpuInterpreter = Interpreter(model, gpuOptions)

                val inputTensor = gpuInterpreter.getInputTensor(0)
                val outputTensor = gpuInterpreter.getOutputTensor(0)

                val dummyInput =
                    ByteBuffer.allocateDirect(inputTensor.numBytes())
                        .order(java.nio.ByteOrder.nativeOrder())

                val dummyOutput =
                    ByteBuffer.allocateDirect(outputTensor.numBytes())
                        .order(java.nio.ByteOrder.nativeOrder())

                gpuInterpreter.run(dummyInput, dummyOutput)

                Log.i(TAG, "GPU delegate WORKS, using GPU")
                return gpuInterpreter

            } catch (e: Throwable) {
                Log.w(TAG, "GPU delegate FAILED, falling back to CPU", e)
            }
        }

        Log.i(TAG, "Using CPU interpreter")
        val cpuOptions = Interpreter.Options().apply {
            setNumThreads(4)
        }

        return Interpreter(model, cpuOptions)
    }


    suspend fun restart() = withContext(Dispatchers.Default) {
        interpreter?.close()
        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = createInterpreter(model)
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    fun isActive(): Boolean = interpreter != null

    // -----------------------------
    // Detección
    // -----------------------------
    suspend fun detect(frame: Bitmap) {
        val localInterpreter = interpreter ?: return

        if (tensorWidth == 0 || tensorHeight == 0) return

        val startTime = SystemClock.uptimeMillis()

        val resized = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false)
        val tensorImage = TensorImage(INPUT_IMAGE_TYPE)
        tensorImage.load(resized)

        val processed = imageProcessor.process(tensorImage)
        val inputBuffer = processed.buffer

        val outputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, numChannel, numElements),
            OUTPUT_IMAGE_TYPE
        )

        localInterpreter.run(inputBuffer, outputBuffer.buffer)

        val boxes = bestBox(outputBuffer.floatArray)
        val inferenceTime = SystemClock.uptimeMillis() - startTime

        _results.emit(
            DetectionResult(
                boxes ?: emptyList(),
                inferenceTime,
                frame
            )
        )
    }

    // -----------------------------
    // Post-procesamiento
    // -----------------------------
    private fun bestBox(array: FloatArray): List<BoundingBox>? {
        val boxes = ArrayList<BoundingBox>()
        val currentLabels = _labels.value

        for (c in 0 until numElements) {
            var maxIdx = -1
            var maxConf = CONFIDENCE_THRESHOLD

            for (j in 4 until numChannel) {
                val idx = (numElements * j) + c
                if (array[idx] > maxConf) {
                    maxConf = array[idx]
                    maxIdx = j - 4
                }
            }

            if (maxIdx in currentLabels.indices) {
                val cx = array[c]
                val cy = array[numElements + c]
                val w = array[(numElements * 2) + c]
                val h = array[(numElements * 3) + c]

                val x1 = cx - w / 2f
                val y1 = cy - h / 2f
                val x2 = cx + w / 2f
                val y2 = cy + h / 2f

                if (x1 in 0f..1f && y1 in 0f..1f && x2 in 0f..1f && y2 in 0f..1f) {
                    boxes.add(
                        BoundingBox(
                            x1, y1, x2, y2,
                            cx, cy, w, h,
                            maxConf, maxIdx,
                            currentLabels[maxIdx]
                        )
                    )
                }
            }
        }

        return if (boxes.isEmpty()) null else applyNMS(boxes)
    }

    private fun applyNMS(boxes: List<BoundingBox>): List<BoundingBox> {
        val sorted = boxes.sortedByDescending { it.cnf }.toMutableList()
        val selected = ArrayList<BoundingBox>()

        while (sorted.isNotEmpty()) {
            val first = sorted.removeAt(0)
            selected.add(first)

            val it = sorted.iterator()
            while (it.hasNext()) {
                val box = it.next()
                if (calculateIoU(first, box) >= IOU_THRESHOLD) {
                    it.remove()
                }
            }
        }
        return selected
    }

    private fun calculateIoU(a: BoundingBox, b: BoundingBox): Float {
        val x1 = max(a.x1, b.x1)
        val y1 = max(a.y1, b.y1)
        val x2 = min(a.x2, b.x2)
        val y2 = min(a.y2, b.y2)

        val inter = max(0f, x2 - x1) * max(0f, y2 - y1)
        val areaA = a.w * a.h
        val areaB = b.w * b.h

        return inter / (areaA + areaB - inter)
    }
}
