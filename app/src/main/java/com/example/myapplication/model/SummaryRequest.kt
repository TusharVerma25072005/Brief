package com.example.myapplication.model

data class SummaryRequest(
    val emailId: String,
    val subject: String,
    val bodyWithDummies: String
)
