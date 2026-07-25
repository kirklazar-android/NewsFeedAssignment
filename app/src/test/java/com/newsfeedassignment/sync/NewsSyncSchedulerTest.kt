package com.newsfeedassignment.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NewsSyncSchedulerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setMinimumLoggingLevel(0).build(),
        )
    }

    @After
    fun tearDown() {
        WorkManager.getInstance(context).cancelUniqueWork(NewsSyncWorker.WORK_NAME)
    }

    @Test
    fun `schedule is unique and requires an unmetered network`() {
        NewsSyncScheduler.schedule(context)
        NewsSyncScheduler.schedule(context)

        val work = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(NewsSyncWorker.WORK_NAME)
            .get()

        assertEquals(1, work.size)
        assertEquals(NetworkType.UNMETERED, work.single().constraints.requiredNetworkType)
    }
}
