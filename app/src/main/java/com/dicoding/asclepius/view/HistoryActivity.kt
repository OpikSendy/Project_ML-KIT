package com.dicoding.asclepius.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.data.PredictionDatabase
import com.dicoding.asclepius.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var predictionDatabase: PredictionDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        predictionDatabase = PredictionDatabase.getDatabase(this)
        historyAdapter = HistoryAdapter()

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }

        loadHistoryData()
    }

    private fun loadHistoryData() {
        predictionDatabase.predictionHistoryDao().getAllHistory().observe(this) { historyList ->
            historyAdapter.submitList(historyList)
        }
    }
}
