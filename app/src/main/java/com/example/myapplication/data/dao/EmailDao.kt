package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.EmailEntity

@Dao
interface EmailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)

    @Query("SELECT * FROM emails ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getEmailsPaged(limit: Int, offset: Int): List<EmailEntity>

    @Query("UPDATE emails SET summary = :summary WHERE id = :emailId")
    suspend fun updateEmailSummary(emailId: String, summary: String)
}

