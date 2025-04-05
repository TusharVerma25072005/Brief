package com.example.myapplication.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.myapplication.BriefyApplication
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.entity.EmailEntity

object NotificationHelper {
    private const val EMAIL_NOTIFICATION_ID = 1001

    fun showNewEmailsNotification(context: Context, emails: List<EmailEntity>) {
        if (emails.isEmpty()) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent for opening the app
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(context, BriefyApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("New Email Summaries")
            .setContentText("${emails.size} new emails have been processed")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Use InboxStyle for multiple emails
        if (emails.size > 1) {
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle("${emails.size} new emails processed")

            // Add up to 5 email previews
            emails.take(5).forEach { email ->
                inboxStyle.addLine("${email.sender}: ${email.subject}")
            }

            if (emails.size > 5) {
                inboxStyle.setSummaryText("+ ${emails.size - 5} more")
            }

            notificationBuilder.setStyle(inboxStyle)
        } else {
            // Just one email - show its details
            val email = emails.first()
            notificationBuilder.setContentTitle(email.sender)
                .setContentText(email.subject)
                .setStyle(NotificationCompat.BigTextStyle().bigText(email.summary ?: email.snippet))
        }

        notificationManager.notify(EMAIL_NOTIFICATION_ID, notificationBuilder.build())
    }
}