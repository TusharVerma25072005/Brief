package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey val id: String,
    val sender: String,
    val subject: String,
    val snippet: String,
    val body: String,
    val summary: String?, // AI-generated summary (nullable)
    val timestamp: Long,
    val read: Boolean
)