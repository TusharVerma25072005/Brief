// Create file: app/src/main/java/com/example/myapplication/network/NetworkModule.kt
package com.example.myapplication.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val GMAIL_BASE_URL = "https://www.googleapis.com/"
    private const val SUMMARY_BASE_URL = "https://your-summary-api-url.com/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })

    fun createGmailService(authToken: String): GmailService {
        val authenticatedClient = httpClient.addInterceptor(Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }).build()

        return Retrofit.Builder()
            .baseUrl(GMAIL_BASE_URL)
            .client(authenticatedClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GmailService::class.java)
    }

    fun createSummaryService(): SummaryService {
        return Retrofit.Builder()
            .baseUrl(SUMMARY_BASE_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SummaryService::class.java)
    }
}