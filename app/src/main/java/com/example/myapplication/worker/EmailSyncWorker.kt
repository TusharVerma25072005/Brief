// Create file: app/src/main/java/com/example/myapplication/worker/EmailSyncWorker.kt
package com.example.myapplication.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.entity.EmailEntity
import com.example.myapplication.model.Email
import com.example.myapplication.model.SummaryRequest
import com.example.myapplication.network.GmailService
import com.example.myapplication.network.NetworkModule
import com.example.myapplication.network.SummaryService
import com.example.myapplication.util.EncryptionUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class EmailSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val emailDao = database.emailDao()
    private val personalDataDao = database.personalDataDao()
    private val summaryService = NetworkModule.createSummaryService()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // This would normally come from a secure storage after OAuth authentication
            val authToken = inputData.getString("auth_token") ?: return@withContext Result.failure()

            val gmailService = NetworkModule.createGmailService(authToken)
            val emails = fetchEmails(gmailService)

            processEmails(emails, gmailService, summaryService)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun fetchEmails(gmailService: GmailService, maxResults: Int = 20): List<Email> {
        val response = gmailService.getMessages(maxResults)
        val emails = mutableListOf<Email>()

        for (message in response.messages) {
            val detail = gmailService.getMessageDetail(message.id)
            val email = parseEmailDetail(detail)
            emails.add(email)
        }

        return emails
    }

    private fun parseEmailDetail(detail: com.example.myapplication.model.EmailDetailResponse): Email {
        val headers = detail.payload.headers
        val sender = headers.find { it.name.equals("From", ignoreCase = true) }?.value ?: "Unknown"
        val subject = headers.find { it.name.equals("Subject", ignoreCase = true) }?.value ?: ""
        val date = headers.find { it.name.equals("Date", ignoreCase = true) }?.value ?: ""

        // Extract body content from payload or parts
        val body = extractBody(detail.payload)

        return Email(
            id = detail.id,
            sender = sender,
            subject = subject,
            snippet = detail.payload.body?.data?.let { decodeBase64(it) } ?: "",
            body = body,
            timeStamp = date
        )
    }

    private fun extractBody(payload: com.example.myapplication.model.EmailPayload): String {
        // Handle plain text
        if (payload.mimeType?.contains("text/plain") == true && payload.body?.data != null) {
            return decodeBase64(payload.body.data)
        }

        // Handle HTML
        if (payload.mimeType?.contains("text/html") == true && payload.body?.data != null) {
            val html = decodeBase64(payload.body.data)
            return extractTextFromHtml(html)
        }

        // Handle multipart
        if (payload.mimeType?.contains("multipart") == true) {
            payload.parts?.forEach { part ->
                if (part.mimeType?.contains("text/plain") == true && part.body?.data != null) {
                    return decodeBase64(part.body.data)
                }
            }

            // Try HTML if no plain text
            payload.parts?.forEach { part ->
                if (part.mimeType?.contains("text/html") == true && part.body?.data != null) {
                    val html = decodeBase64(part.body.data)
                    return extractTextFromHtml(html)
                }
            }
        }

        return ""
    }

    private fun decodeBase64(data: String): String {
        return try {
            String(android.util.Base64.decode(data.replace('-', '+').replace('_', '/'), android.util.Base64.DEFAULT))
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractTextFromHtml(html: String): String {
        // Very basic HTML to text conversion
        return html.replace("<[^>]*>".toRegex(), " ")
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private suspend fun processEmails(
        emails: List<Email>,
        gmailService: GmailService,
        summaryService: SummaryService
    ) {
        val emailEntities = mutableListOf<EmailEntity>()

        for (email in emails) {
            // Process for sensitive data
            val body = email.body ?: ""
            val (processedBody, encryptedData) = EncryptionUtil.processText(body)

            // Store encrypted data
            if (encryptedData.isNotEmpty()) {
                personalDataDao.insertPersonalData(
                    com.example.myapplication.data.entity.PersonalDataEntity(
                        emailId = email.id,
                        extractedData = Gson().toJson(encryptedData)
                    )
                )
            }

            // Get summary from server
            val summaryRequest = SummaryRequest(
                emailId = email.id,
                subject = email.subject,
                bodyWithDummies = processedBody
            )

            try {
                val summaryResponse = summaryService.getSummary(summaryRequest)

                // Restore sensitive data in summary
                val restoredSummary = if (encryptedData.isNotEmpty()) {
                    EncryptionUtil.restoreText(summaryResponse.summary, encryptedData)
                } else {
                    summaryResponse.summary
                }

                // Convert to timestamp
                val parser = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US)
                val timestamp = try {
                    parser.parse(email.timeStamp)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                // Create entity
                val emailEntity = EmailEntity(
                    id = email.id,
                    sender = email.sender,
                    subject = email.subject,
                    snippet = email.snippet,
                    body = body,
                    summary = restoredSummary,
                    timestamp = timestamp,
                    read = email.read
                )

                emailEntities.add(emailEntity)
            } catch (e: Exception) {
                // If summary fails, still save the email without summary
                val timestamp = System.currentTimeMillis()

                val emailEntity = EmailEntity(
                    id = email.id,
                    sender = email.sender,
                    subject = email.subject,
                    snippet = email.snippet,
                    body = body,
                    summary = null,
                    timestamp = timestamp,
                    read = email.read
                )

                emailEntities.add(emailEntity)
            }
        }

        // Save all processed emails
        if (emailEntities.isNotEmpty()) {
            emailDao.insertEmails(emailEntities)
        }
    }
}