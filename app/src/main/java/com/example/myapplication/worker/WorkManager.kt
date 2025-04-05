// Create file: app/src/main/java/com/example/myapplication/worker/WorkManager.kt
package com.example.myapplication.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManager {
    private const val EMAIL_SYNC_WORK = "email_sync_work"

    fun scheduleEmailSync(context: Context, authToken: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val inputData = Data.Builder()
            .putString("auth_token", authToken)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<EmailSyncWorker>(
            15, TimeUnit.MINUTES, // Minimum interval is 15 minutes
            5, TimeUnit.MINUTES // Flex period
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EMAIL_SYNC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    fun cancelEmailSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(EMAIL_SYNC_WORK)
    }
}