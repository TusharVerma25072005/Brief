package com.example.myapplication.model

data class User(
    val id : String,
    val name : String ,
    val profilePicture : String? = null,
    val email : String,
)
