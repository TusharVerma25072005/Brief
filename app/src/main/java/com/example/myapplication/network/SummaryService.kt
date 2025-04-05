
package com.example.myapplication.network

import com.example.myapplication.model.SummarizedEmail
import com.example.myapplication.model.SummaryRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface SummaryService {
    @POST("summarize")
    suspend fun getSummary(@Body request: SummaryRequest): SummarizedEmail
}