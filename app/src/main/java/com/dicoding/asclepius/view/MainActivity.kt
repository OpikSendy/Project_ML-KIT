package com.dicoding.asclepius.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.currentImageUri.observe(this) { uri ->
            uri?.let {
                binding.previewImageView.setImageURI(it)
                binding.analyzeButton.isEnabled = true
            }
        }
    }

    private fun setupButtons() {
        setupButtonAnimation(binding.galleryButton) { startGallery() }
        setupButtonAnimation(binding.analyzeButton) { analyzeImage() }
        setupButtonAnimation(binding.historyButton) { openHistoryPage() }
        setupButtonAnimation(binding.btnOpenNews) { openNewsPage() }
    }

    private fun setupButtonAnimation(button: Button, action: () -> Unit) {
        button.setOnClickListener {
            val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                button,
                PropertyValuesHolder.ofFloat("scaleX", 0.95f),
                PropertyValuesHolder.ofFloat("scaleY", 0.95f)
            ).apply {
                duration = 100
                repeatCount = 1
                repeatMode = ValueAnimator.REVERSE
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        action()
                    }
                })
            }
            scaleDown.start()
        }
    }

    private fun openHistoryPage() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    private fun openNewsPage() {
        val intent = Intent(this, NewsActivity::class.java)
        startActivity(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Pilih gambar")
        launcherGallery.launch(chooser)
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data
            selectedImg?.let { uri ->
                startCrop(uri)
            }
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        val destinationUri = Uri.fromFile(
            File(
                cacheDir,
                "${imageFileName}.jpg"
            )
        )

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(80)
                setHideBottomControls(false)
                setFreeStyleCropEnabled(true)
                setToolbarColor(ContextCompat.getColor(this@MainActivity, R.color.purple_500))
                setStatusBarColor(ContextCompat.getColor(this@MainActivity, R.color.purple_700))
                setToolbarTitle("Crop Gambar")
            })

        try {
            uCrop.start(this)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error saat memulai crop: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            viewModel.setImageUri(resultUri) // Menggunakan ViewModel untuk menyimpan URI
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            showToast("Error saat crop: ${cropError?.message}")
        }
    }

    private fun analyzeImage() {
        if (viewModel.hasImageSelected()) {
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("image_uri", viewModel.currentImageUri.value.toString())
            }
            startActivity(intent)
        } else {
            showToast("Pilih gambar terlebih dahulu")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}