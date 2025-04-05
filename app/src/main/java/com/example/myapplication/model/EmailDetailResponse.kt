package com.example.myapplication.model

data class EmailDetailResponse(
    val id : String,
    val threadId : String,
    val payload : EmailPayload, // contains body
)

data class EmailPayload(
    val headers : List<EmailHeader>,
    val body: EmailBody?,
    val mimeType: String?,
    val parts: List<EmailPayload>? = null
)

data class EmailHeader(
    val name : String,
    val value : String,
)

data class EmailBody(
    val data : String?,
)

