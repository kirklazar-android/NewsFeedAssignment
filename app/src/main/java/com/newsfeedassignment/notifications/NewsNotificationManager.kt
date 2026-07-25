package com.newsfeedassignment.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.newsfeedassignment.MainActivity
import com.newsfeedassignment.core.model.Article

data class NewsNotificationContent(
    val title: String,
    val summary: String,
    val lines: List<String>,
)

object NewsNotificationContentFactory {
    fun from(articles: List<Article>): NewsNotificationContent? {
        if (articles.isEmpty()) return null
        val lines = articles.take(MAX_HEADLINES).map { it.title }
        return NewsNotificationContent(
            title = "NewsFeedAssignment",
            summary = "${articles.size} new ${if (articles.size == 1) "article" else "articles"}",
            lines = lines,
        )
    }

    private const val MAX_HEADLINES = 3
}

class NewsNotificationManager @Inject constructor(@ApplicationContext private val context: Context) {
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifications for new news articles"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun showNewArticles(articles: List<Article>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val content = NewsNotificationContentFactory.from(articles) ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val inboxStyle = NotificationCompat.InboxStyle().setSummaryText(content.summary)
        content.lines.forEach(inboxStyle::addLine)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(content.title)
            .setContentText(content.summary)
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "news_updates"
        const val CHANNEL_NAME = "News updates"
        private const val NOTIFICATION_ID = 1001
    }
}
