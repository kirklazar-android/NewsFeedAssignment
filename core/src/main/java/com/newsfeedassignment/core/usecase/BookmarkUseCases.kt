package com.newsfeedassignment.core.usecase

import com.newsfeedassignment.core.model.Article
import com.newsfeedassignment.core.model.Bookmark
import com.newsfeedassignment.core.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow

class ObserveBookmarks(private val repository: BookmarkRepository) {
    operator fun invoke(): Flow<List<Bookmark>> = repository.observeBookmarks()
}

class SearchBookmarks(private val repository: BookmarkRepository) {
    operator fun invoke(query: String): Flow<List<Bookmark>> = repository.searchBookmarks(query.trim())
}

class ObserveBookmarkState(private val repository: BookmarkRepository) {
    operator fun invoke(articleId: String): Flow<Boolean> = repository.isBookmarked(articleId)
}

class SaveBookmark(private val repository: BookmarkRepository) {
    suspend operator fun invoke(article: Article): Result<Unit> = repository.saveBookmark(article)
}

class RemoveBookmark(private val repository: BookmarkRepository) {
    suspend operator fun invoke(articleId: String): Result<Unit> = repository.removeBookmark(articleId)
}