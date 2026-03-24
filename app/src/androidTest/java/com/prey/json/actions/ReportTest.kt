/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.prey.PreyConfig
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ReportTest {

    private var context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var config: PreyConfig

    @Before
    fun setup() {
        config = PreyConfig.getPreyConfig(context)
    }

    @Test
    fun test_report_start() = runBlocking {
        config.deleteActions()
        val options = JSONObject().apply {
            put("interval", 20)
        }
        Report.get(context, options)
        Thread.sleep(1000)
        Assert.assertTrue(config.isMissing)
        Assert.assertEquals(config.intervalReport, "20")
    }

    @Test
    fun test_report_stop() {
        val options = JSONObject()
        Report.stop(context, options)
        Assert.assertFalse(config.isMissing)
        Assert.assertEquals(config.intervalReport, "")
    }

}