package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val viewModel: ResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aktifkan rotasi layar
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        setupObservers()
        processIntent(savedInstanceState)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.resultLayout.isVisible = !isLoading
        }

        viewModel.resultState.observe(this) { state ->
            when (state) {
                is ResultViewModel.ResultState.Success -> {
                    displaySuccess(state)
                }
                is ResultViewModel.ResultState.Error -> {
                    showError(state.message)
                }
                is ResultViewModel.ResultState.Initial -> {
                    // Do nothing or show initial state
                }
            }
        }
    }

    private fun processIntent(savedInstanceState: Bundle?) {
        val imageUriString = if (savedInstanceState != null) {
            // Restore from saved state if available
            savedInstanceState.getString("image_uri")
        } else {
            // Get from intent if no saved state
            intent.getStringExtra("image_uri")
        }

        if (imageUriString != null) {
            try {
                val imageUri = Uri.parse(imageUriString)
                viewModel.processImage(imageUri)
            } catch (e: Exception) {
                showError("Invalid image URI: ${e.message}")
            }
        } else {
            showError("No image provided")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Simpan URI gambar ke saved instance state
        intent.getStringExtra("image_uri")?.let { uri ->
            outState.putString("image_uri", uri)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displaySuccess(state: ResultViewModel.ResultState.Success) {
        binding.apply {
            resultLayout.isVisible = true

            // Load image using Glide untuk handling yang lebih baik
            Glide.with(this@ResultActivity)
                .load(state.imageUri)
                .into(resultImage)

            // Update result text
            resultText.text = "Hasil Diagnosis: ${state.result.label}"
            confidenceText.text = "Tingkat Kepercayaan: ${state.confidencePercentage}"

            // Set color based on result
            val textColor = if (state.result.label == "Cancer") Color.RED else Color.GREEN
            resultText.setTextColor(textColor)
        }
    }

    private fun showError(message: String) {
        binding.apply {
            resultLayout.isVisible = true
            resultText.text = "Error"
            confidenceText.text = message
            resultText.setTextColor(Color.RED)
        }
    }
}