package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.R
import com.dicoding.asclepius.data.PredictionHistory
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<PredictionHistory, HistoryAdapter.HistoryViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prediction, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = getItem(position)
        holder.bind(historyItem)
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resultText: TextView = itemView.findViewById(R.id.resultTextHistory)
        private val confidenceText: TextView = itemView.findViewById(R.id.confidenceTextHistory)
        private val timestampText: TextView = itemView.findViewById(R.id.timestampTextHistory)
        private val historyImage: ImageView = itemView.findViewById(R.id.historyImage)

        @SuppressLint("SetTextI18n")
        fun bind(history: PredictionHistory) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

            resultText.text = "Hasil Diagnosis: ${history.result}"  // Tambahkan label yang jelas
            confidenceText.text = "Kepercayaan: %.1f%%".format(history.confidence * 100)
            timestampText.text = "Waktu: ${dateFormat.format(Date(history.timestamp))}"

            Glide.with(itemView.context)
                .load(Uri.parse(history.imageUri))
                .into(historyImage)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PredictionHistory>() {
        override fun areItemsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean {
            return oldItem == newItem
        }
    }
}

