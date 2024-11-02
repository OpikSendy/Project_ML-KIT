package com.dicoding.asclepius.view

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.data.PredictionDatabase
import com.dicoding.asclepius.data.PredictionHistory
import com.dicoding.asclepius.helper.ClassificationResult
import com.dicoding.asclepius.helper.ImageClassifierHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultViewModel(application: Application) : AndroidViewModel(application) {
    private val predictionDatabase = PredictionDatabase.getDatabase(application)
    private val classifierHelper = ImageClassifierHelper(application)

    // Saved state untuk menangani rotasi
    private var savedImageUri: Uri? = null
    private var savedResult: ClassificationResult? = null

    private val _resultState = MutableLiveData<ResultState>()
    val resultState: LiveData<ResultState> = _resultState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    sealed class ResultState {
        data class Success(
            val imageUri: Uri,
            val result: ClassificationResult,
            val confidencePercentage: String
        ) : ResultState()
        data class Error(val message: String) : ResultState()
        object Initial : ResultState()
    }

    init {
        // Memulihkan state jika ada
        restoreState()
    }

    private fun restoreState() {
        savedImageUri?.let { uri ->
            savedResult?.let { result ->
                val confidencePercentage = "%.1f%%".format(result.confidence * 100)
                _resultState.value = ResultState.Success(
                    imageUri = uri,
                    result = result,
                    confidencePercentage = confidencePercentage
                )
            }
        }
    }

    fun processImage(imageUri: Uri, forceReprocess: Boolean = false) {
        // Jika sudah ada hasil dan tidak diminta untuk memproses ulang, kembalikan hasil yang ada
        if (!forceReprocess && savedImageUri == imageUri && savedResult != null) {
            restoreState()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bitmap = getBitmapFromUri(imageUri)
                val result = classifierHelper.classifyImage(bitmap)

                if (result.isSuccess) {
                    // Simpan state
                    savedImageUri = imageUri
                    savedResult = result

                    val confidencePercentage = "%.1f%%".format(result.confidence * 100)
                    _resultState.value = ResultState.Success(
                        imageUri = imageUri,
                        result = result,
                        confidencePercentage = confidencePercentage
                    )

                    // Save prediction to database
                    savePredictionHistory(imageUri, result.label, result.confidence)
                } else {
                    _resultState.value = ResultState.Error(
                        result.errorMessage ?: "Unknown error occurred"
                    )
                }
            } catch (e: Exception) {
                _resultState.value = ResultState.Error("Error processing image: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun savePredictionHistory(
        imageUri: Uri,
        result: String,
        confidence: Float
    ) {
        withContext(Dispatchers.IO) {
            val history = PredictionHistory(
                imageUri = imageUri.toString(),
                result = result,
                confidence = confidence
            )
            predictionDatabase.predictionHistoryDao().insert(history)
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(getApplication<Application>().contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true // Memastikan bitmap bisa dimutasi
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(getApplication<Application>().contentResolver, uri)
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifierHelper.close()
    }
}