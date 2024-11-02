package com.dicoding.asclepius.data

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("everything")
    suspend fun getHealthNews(
        @Query("q") query: String = "cancer health",
        @Query("apiKey") apiKey: String = "e07f25cfc2044009b0cbfb5387c61951",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt"
    ): NewsResponse
}