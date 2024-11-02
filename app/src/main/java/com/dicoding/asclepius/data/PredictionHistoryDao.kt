package com.dicoding.asclepius.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PredictionHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PredictionHistory)

    @Query("SELECT * FROM prediction_history ORDER BY timestamp DESC")
    fun getAllHistory(): LiveData<List<PredictionHistory>>

//    @Delete
//    suspend fun delete(history: PredictionHistory)
}
