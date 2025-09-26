package com.example.nougatbora

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://warehouse-backend-ru6r.onrender.com/"

    // Add proper timeout configuration
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // 60 seconds to connect
        .readTimeout(60, TimeUnit.SECONDS)     // 60 seconds to read response
        .writeTimeout(60, TimeUnit.SECONDS)    // 60 seconds to write request
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // See what's being sent/received
        })
        .build()

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Use our custom client with timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}