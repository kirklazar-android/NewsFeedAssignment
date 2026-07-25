package com.newsfeedassignment.sync

import com.newsfeedassignment.core.model.NewsError
import org.junit.Assert.assertEquals
import androidx.work.ListenableWorker
import org.junit.Test

class NewsSyncWorkerTest {
    @Test
    fun `network and server failures request retry`() {
        assertEquals(ListenableWorker.Result.retry().javaClass, NewsSyncWorker.resultFor(NewsError.Network).javaClass)
        assertEquals(ListenableWorker.Result.retry().javaClass, NewsSyncWorker.resultFor(NewsError.Http(503)).javaClass)
    }

    @Test
    fun `authentication and quota failures stop without retry`() {
        assertEquals(ListenableWorker.Result.failure().javaClass, NewsSyncWorker.resultFor(NewsError.Authentication).javaClass)
        assertEquals(ListenableWorker.Result.failure().javaClass, NewsSyncWorker.resultFor(NewsError.Quota).javaClass)
    }
}
