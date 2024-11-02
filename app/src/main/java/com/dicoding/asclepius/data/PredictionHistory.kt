package com.dicoding.asclepius.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prediction_history")
data class PredictionHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageUri: String,
    val result: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)
