package com.dicoding.asclepius.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.data.Article
import com.dicoding.asclepius.data.Instance
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val _news = MutableLiveData<List<Article>>()
    val news: LiveData<List<Article>> = _news

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = Instance.newsApi.getHealthNews()
                if (response.status == "ok") {
                    // Menghapus duplikat berdasarkan title dan url
                    val uniqueArticles = response.articles.groupBy { it.title to it.url }
                        .map { it.value.first() }
                    _news.value = uniqueArticles
                } else {
                    _error.value = "Failed to fetch news"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}