package com.dicoding.asclepius.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Instance {
    private const val BASE_URL = "https://newsapi.org/v2/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val newsApi: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }
}