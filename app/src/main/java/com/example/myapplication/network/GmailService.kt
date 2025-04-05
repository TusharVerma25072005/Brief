package com.example.myapplication.network



import com.example.myapplication.model.EmailDetailResponse
import com.example.myapplication.model.GmailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GmailService {
    @GET("gmail/v1/users/me/messages")
    suspend fun getMessages(
        @Query("maxResults") maxResults: Int,
        @Query("pageToken") pageToken: String? = null
    ): GmailResponse

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessageDetail(@Path("id") messageId: String): EmailDetailResponse
}