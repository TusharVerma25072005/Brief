package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.EmailDao
import com.example.myapplication.data.dao.PersonalDataDao
import com.example.myapplication.data.entity.EmailEntity
import com.example.myapplication.data.entity.PersonalDataEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EmailRepository(
    private val emailDao: EmailDao,
    private val personalDataDao: PersonalDataDao
) {

    suspend fun insertEmails(emails: List<EmailEntity>) {
        emailDao.insertEmails(emails)
    }
    fun getEmailsPaged(limit : Int, offset : Int) : Flow<List<EmailEntity>> = flow{
        emit(emailDao.getEmailsPaged(limit, offset))
    }
    suspend fun storePersonalData(emailId: String, extractedData: String) {
        personalDataDao.insertPersonalData(PersonalDataEntity(emailId, extractedData))
    }
    fun retrievePersonalData(emailId: String) : Flow<String?> = flow{
        emit(personalDataDao.getPersonalData(emailId))
    }
    suspend fun removePersonalData(emailId : String) {
        personalDataDao.deletePersonalData(emailId)
    }
    suspend fun updateEmailSummary(emailId: String, summary: String) {
        emailDao.updateEmailSummary(emailId, summary)
    }

}