package com.example.myapplication.model

data class Email(
    val id : String,
    val sender : String,
    val subject : String,
    var snippet : String,
    val body : String?,
    val timeStamp : String,
    var read : Boolean = false,
)
