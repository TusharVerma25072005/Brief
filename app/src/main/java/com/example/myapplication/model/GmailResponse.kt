package com.example.myapplication.model

data class GmailResponse(
    val messages : List<MessageItem>,
    val nextPageToken : String,
    val resultSizeEstimate: Int? = null
)

data class MessageItem(
    val id: String,
    val threadId : String,
)
