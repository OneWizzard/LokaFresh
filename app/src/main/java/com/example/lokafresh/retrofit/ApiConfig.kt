package com.example.lokafresh.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {

    private const val BASE_URL = "http://34.143.173.201:8502/"

    fun getApiService(): ApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS) // Timeout saat membuat koneksi
            .readTimeout(300, TimeUnit.SECONDS)    // Timeout saat membaca respon
            .writeTimeout(300, TimeUnit.SECONDS)   // Timeout saat menulis data
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}