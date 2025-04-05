package com.example.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repository.EmailRepository

class BriefyApplication : Application(), Configuration.Provider {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val repository: EmailRepository by lazy {
        EmailRepository(database.emailDao(), database.personalDataDao())
    }

    // Changed from a method to a property with getter
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Email Notifications"
            val descriptionText = "Notifications for new email summaries"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "briefy_notification_channel"
    }
}