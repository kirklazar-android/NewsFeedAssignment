package com.newsfeedassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.newsfeedassignment.core.repository.BookmarkRepository
import com.newsfeedassignment.core.repository.NewsRepository
import com.newsfeedassignment.feature.news.NewsFeedAssignmentApp
import com.newsfeedassignment.feature.news.detail.AndroidActivityStarter
import com.newsfeedassignment.notifications.NotificationPermissionPrompt
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var newsRepository: NewsRepository
    @Inject lateinit var bookmarkRepository: BookmarkRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NewsFeedAssignmentApp(newsRepository, bookmarkRepository, AndroidActivityStarter(this))
                NotificationPermissionPrompt()
            }
        }
    }
}
