package com.newsfeedassignment.data.sync

import android.content.Context
import androidx.room.Room
import com.newsfeedassignment.core.model.FeedRequest
import com.newsfeedassignment.data.network.GNewsApi
import com.newsfeedassignment.data.network.GNewsArticle
import com.newsfeedassignment.data.network.GNewsResponse
import com.newsfeedassignment.data.network.GNewsSource
import com.newsfeedassignment.data.persistence.NewsDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class NewsFeedRefresherTest {
    private lateinit var database: NewsDatabase
    private lateinit var api: FakeApi
    private lateinit var refresher: NewsFeedRefresher

    @Before
    fun setUp() {
        val context: Context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        api = FakeApi()
        refresher = NewsFeedRefresher(api, database) { 1_000L }
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `first refresh establishes a silent baseline`() = runTest {
        api.response = response("first-0")

        val result = refresher.refresh()

        assertTrue(result.isBaseline)
        assertTrue(result.newArticles.isEmpty())
        assertEquals(2, database.feedMetadataDao().get()?.nextPage)
    }

    @Test
    fun `later refresh returns only unseen articles`() = runTest {
        api.response = response("first-0")
        refresher.refresh()
        api.response = response("second-0", "first-0")

        val result = refresher.refresh(FeedRequest())

        assertFalse(result.isBaseline)
        assertEquals(listOf("second-0"), result.newArticles.map { it.id })
        assertEquals(listOf("second-0", "first-0"), database.cachedArticleDao().all().map { it.id })
    }

    @Test
    fun `empty response does not produce new articles`() = runTest {
        api.response = GNewsResponse(totalArticles = 0)

        val result = refresher.refresh()

        assertTrue(result.isBaseline)
        assertTrue(result.newArticles.isEmpty())
        assertEquals(0, database.cachedArticleDao().count())
    }

    @Test
    fun `network errors are classified for worker retry`() = runTest {
        api.failure = IOException("offline")

        val error = try {
            refresher.refresh()
            null
        } catch (exception: NewsSyncException) {
            exception
        }

        assertEquals(com.newsfeedassignment.core.model.NewsError.Network, error?.error)
    }

    private fun response(vararg ids: String) = GNewsResponse(
        totalArticles = ids.size,
        articles = ids.map { id ->
            GNewsArticle(
                id = id,
                title = "Headline $id",
                url = "https://example.com/$id",
                source = GNewsSource("Source"),
            )
        },
    )

    private class FakeApi : GNewsApi {
        var response: GNewsResponse = GNewsResponse()
        var failure: Throwable? = null

        override suspend fun topHeadlines(country: String, language: String, category: String?, page: Int, max: Int): GNewsResponse {
            failure?.let { throw it }
            return response
        }

        override suspend fun search(query: String, language: String, country: String, page: Int, max: Int): GNewsResponse = error("not used")
    }
}
