package com.prey.actions.location.daily

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class DailyLocationUtilTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        //Initialize WorkManager in test mode
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun calculateDelayUntil0030_isCorrect() {
        //We obtain the calculated delay
        val delay = DailyLocationUtil.calculateDelayUntil0030()
        //We created a schedule to verify the result.
        val target = Calendar.getInstance()
        target.timeInMillis = System.currentTimeMillis() + delay
        //We verified that the resulting time is 00:30
        assertEquals("It should be zero hour", 0, target.get(Calendar.HOUR_OF_DAY))
        assertEquals("It should be the 30th minute.", 30, target.get(Calendar.MINUTE))
        //The delay must always be positive (maximum 24h)
        assertTrue("The delay must be greater than 0", delay > 0)
        assertTrue("The delay should not exceed 24 hours", delay <= 24 * 60 * 60 * 1000)
    }

    @Test
    fun enqueueDailyCheck_isEnqueuedSuccessfully() {
        val workManager = WorkManager.getInstance(context)
        //We execute the function to be tested
        DailyLocationUtil.enqueueDailyCheck(context)
        //We retrieved the information for the single task by its name.
        val workInfos = workManager.getWorkInfosForUniqueWork(DailyLocationUtil.DAILY_LOCATION_WORK_PERIODIC).get()
        //Verifications
        assertEquals("There should be a queued task", 1, workInfos.size)
        val workInfo = workInfos[0]
        assertTrue(
            "The task must be in the ENQUEUED or RUNNING state.",
            workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING
        )
    }

}