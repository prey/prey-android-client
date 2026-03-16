/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import org.junit.Test
import org.junit.Assert.*
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

class DailyLocationWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockkObject(DailyStore)
        mockkObject(PreyWebServicesKt)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun  doWork_returns_Success_if_it_was_already_sent_today() = runTest {
        //We simulated that it was already sent today
        every { DailyStore.wasSentToday(any()) } returns true
        val worker = TestListenableWorkerBuilder<DailyLocationWorker>(context).build()
        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Success)
        //We verified that the web service was not called
        verify(exactly = 0) { PreyWebServicesKt.sendDailyLocation(any(), any()) }
    }

}