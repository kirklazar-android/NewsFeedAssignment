package com.newsfeedassignment.data.sync

import com.newsfeedassignment.core.model.Article
import com.newsfeedassignment.core.model.FeedRequest
import com.newsfeedassignment.core.model.NewsError
import com.newsfeedassignment.data.network.GNewsApi
import com.newsfeedassignment.data.network.GNewsConfig
import com.newsfeedassignment.data.network.toDomain as toArticle
import com.newsfeedassignment.data.network.toNewsError
import com.newsfeedassignment.data.persistence.FeedMetadataEntity
import com.newsfeedassignment.data.persistence.NewsDatabase
import com.newsfeedassignment.data.persistence.toCachedEntity
import com.newsfeedassignment.data.persistence.toDomain as toCachedArticle
import java.io.IOException

class NewsSyncException(val error: NewsError) : IOException(error.toString())

data class SyncResult(
    val isBaseline: Boolean,
    val newArticles: List<Article>,
)

class NewsFeedRefresher(
    private val api: GNewsApi,
    private val database: NewsDatabase,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    suspend fun refresh(request: FeedRequest = FeedRequest()): SyncResult {
        val existingArticles = database.cachedArticleDao().all()
        val previousMetadata = database.feedMetadataDao().get()
        val hasBaseline = previousMetadata?.lastSuccessfulRefresh != null
        val refreshedAt = nowMillis()

        val response = try {
            api.topHeadlines(
                country = request.country,
                language = request.language,
                category = request.category,
                page = 1,
                max = GNewsConfig.FREE_PLAN_PAGE_SIZE,
            )
        } catch (error: NewsSyncException) {
            throw error
        } catch (error: Throwable) {
            throw NewsSyncException(error.toNewsError())
        }

        val articles = try {
            response.articles.mapIndexed { index, article ->
                article.toArticle(index).toCachedEntity(refreshedAt)
            }
        } catch (error: IllegalArgumentException) {
            throw NewsSyncException(NewsError.Validation)
        }

        val endReached = articles.isEmpty() ||
            articles.size < GNewsConfig.FREE_PLAN_PAGE_SIZE ||
            GNewsConfig.FREE_PLAN_PAGE_SIZE >= response.totalArticles
        val metadata = FeedMetadataEntity(
            requestKey = request.key,
            nextPage = 2,
            endOfPagination = endReached,
            lastSuccessfulRefresh = refreshedAt,
        )

        val existingIds = existingArticles.asSequence().map { it.id }.toSet()
        val newArticles = if (hasBaseline) {
            articles.filterNot { it.id in existingIds }.map { it.toCachedArticle() }
        } else {
            emptyList()
        }

        database.replaceFeed(articles, metadata)
        return SyncResult(isBaseline = !hasBaseline, newArticles = newArticles)
    }
}
