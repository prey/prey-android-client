/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import androidx.test.core.app.ApplicationProvider
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DailyLocationUtilTest {

    @Before
    fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun enqueueDailyCheck_schedules_work_with_internet_constraint() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        DailyLocationUtil.enqueueDailyCheck(context)
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork("daily_location_work").get()
        val workSpec = workInfos[0]
        //We verified that it requires a network connection
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
    }
}