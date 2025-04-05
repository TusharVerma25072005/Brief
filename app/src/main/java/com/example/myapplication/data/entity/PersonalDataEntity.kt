package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_data")
data class PersonalDataEntity(
    @PrimaryKey val emailId : String,
    val extractedData : String
)
