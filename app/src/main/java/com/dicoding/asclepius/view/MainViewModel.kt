package com.dicoding.asclepius.view

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    // MutableLiveData untuk menyimpan URI gambar
    private val _currentImageUri = MutableLiveData<Uri?>()
    val currentImageUri: LiveData<Uri?> = _currentImageUri

    // Fungsi untuk mengupdate URI gambar
    fun setImageUri(uri: Uri?) {
        _currentImageUri.value = uri
    }

    // Fungsi untuk mengecek apakah ada gambar yang dipilih
    fun hasImageSelected(): Boolean {
        return currentImageUri.value != null
    }
}