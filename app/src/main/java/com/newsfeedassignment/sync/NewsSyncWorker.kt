package com.newsfeedassignment.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.newsfeedassignment.core.model.NewsError
import com.newsfeedassignment.data.network.toNewsError
import com.newsfeedassignment.data.sync.NewsFeedRefresher
import com.newsfeedassignment.data.sync.NewsSyncException
import com.newsfeedassignment.notifications.NewsNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NewsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val refresher: NewsFeedRefresher,
    private val notifications: NewsNotificationManager,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = try {
        val preferences = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isFirstBackgroundSync = !preferences.getBoolean(BASELINE_ESTABLISHED_KEY, false)
        val result = refresher.refresh()
        if (!isFirstBackgroundSync && !result.isBaseline) notifications.showNewArticles(result.newArticles)
        preferences.edit().putBoolean(BASELINE_ESTABLISHED_KEY, true).apply()
        Result.success()
    } catch (error: NewsSyncException) {
        resultFor(error.error)
    } catch (error: Throwable) {
        resultFor(error.toNewsError())
    }

    companion object {
        const val WORK_NAME = "news-feed-periodic-sync"
        private const val PREFERENCES_NAME = "news_sync_preferences"
        private const val BASELINE_ESTABLISHED_KEY = "background_baseline_established"

        fun resultFor(error: NewsError): Result = when (error) {
            NewsError.Network -> Result.retry()
            is NewsError.Http -> if (error.retryable) Result.retry() else Result.failure()
            else -> Result.failure()
        }
    }
}
