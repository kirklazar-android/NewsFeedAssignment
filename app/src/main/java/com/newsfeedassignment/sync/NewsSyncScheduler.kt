package com.newsfeedassignment.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.newsfeedassignment.sync.NewsSyncWorker.Companion.WORK_NAME
import java.util.concurrent.TimeUnit

object NewsSyncScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<NewsSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
