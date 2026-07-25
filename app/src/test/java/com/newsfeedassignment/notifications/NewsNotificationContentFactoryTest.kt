package com.newsfeedassignment.notifications

import com.newsfeedassignment.core.model.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsNotificationContentFactoryTest {
    @Test
    fun `empty article list creates no notification`() {
        assertNull(NewsNotificationContentFactory.from(emptyList()))
    }

    @Test
    fun `notification summarizes articles and limits headline lines`() {
        val content = NewsNotificationContentFactory.from((1..4).map(::article))

        requireNotNull(content)
        assertEquals("4 new articles", content.summary)
        assertEquals(listOf("Headline 1", "Headline 2", "Headline 3"), content.lines)
    }

    private fun article(index: Int) = Article(
        id = index.toString(),
        title = "Headline $index",
        description = null,
        content = null,
        sourceName = "Source",
        imageUrl = null,
        originalUrl = "https://example.com/$index",
        publishedAt = null,
    )
}
