package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.PersonalDataEntity

@Dao
interface PersonalDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalData(data: PersonalDataEntity)

    @Query("SELECT extractedData FROM personal_data WHERE emailId = :emailId")
    suspend fun getPersonalData(emailId: String): String?

    @Query("DELETE FROM personal_data WHERE emailId = :emailId")
    suspend fun deletePersonalData(emailId: String)
}
