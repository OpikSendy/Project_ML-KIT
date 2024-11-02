package com.dicoding.asclepius.helper

data class ClassificationResult(
    val label: String,
    val confidence: Float,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)