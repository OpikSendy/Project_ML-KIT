package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel


class ImageClassifierHelper(
    private val context: Context,
    private val modelName: String = "cancer_classification.tflite"
) {
    private var interpreter: Interpreter? = null
    private val imageSize = 224 // Sesuaikan dengan input size model Anda
    private val labels = arrayOf("Non-Cancer", "Cancer") // Sesuaikan dengan label model Anda

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val model = loadModelFile()
        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)
    }

    private fun loadModelFile(): ByteBuffer {
        val modelPath = modelName
        val assetManager = context.assets
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classifyImage(bitmap: Bitmap): ClassificationResult {
        try {
            // Resize bitmap
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)

            // Convert bitmap to ByteBuffer
            val inputBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            inputBuffer.order(ByteOrder.nativeOrder())

            // Get pixels array
            val pixels = IntArray(imageSize * imageSize)
            scaledBitmap.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)

            // Process pixels and load them into input buffer
            for (pixel in pixels) {
                // Extract RGB values and normalize to [-1, 1]
                inputBuffer.putFloat(((pixel shr 16 and 0xFF) - 127.5f) / 127.5f)
                inputBuffer.putFloat(((pixel shr 8 and 0xFF) - 127.5f) / 127.5f)
                inputBuffer.putFloat(((pixel and 0xFF) - 127.5f) / 127.5f)
            }

            // Run inference
            val outputBuffer = Array(1) { FloatArray(labels.size) }
            interpreter?.run(inputBuffer, outputBuffer)

            // Get results
            val results = outputBuffer[0]
            val maxIndex = results.indices.maxByOrNull { results[it] } ?: 0
            val confidence = results[maxIndex]

            return ClassificationResult(
                label = labels[maxIndex],
                confidence = confidence,
                isSuccess = true
            )
        } catch (e: Exception) {
            return ClassificationResult(
                label = "Error",
                confidence = 0f,
                isSuccess = false,
                errorMessage = e.message ?: "Unknown error occurred"
            )
        }
    }

    fun close() {
        interpreter?.close()
    }
}