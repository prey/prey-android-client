/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyStatus
import org.json.JSONObject
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmTest {

    private lateinit var alarm: Alarm
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        alarm = Alarm()
    }

    @Test
    fun testAlarmStatusChangesToStarted() {
        PreyStatus.getInstance().setAlarmStop()
        val options = JSONObject().apply {
            put("job_id", "job_001")
        }
        alarm.execute(context, BaseAction.CMD_START, options)
        Thread.sleep(2000)
        assertTrue("The state of alarm should be 'Started'",
            PreyStatus.getInstance().isAlarmStart
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testUnknownCommandThrowsException() {
        alarm.execute(context, "invalid_command", JSONObject())
    }

    @Test
    fun test_alarm_ok() {
        //We ensure that the initial state is stopped
        PreyStatus.getInstance().setAlarmStop()
        val config = PreyConfig.getPreyConfig(context)
        config.deleteActions();
        val options = JSONObject().apply {
            put("message_id", "12345")
            put("sound", "siren")
        }
        PreyLogger.d("test location.get(context, jsonObjet)")
        alarm.execute(context, "start", options)
        Thread.sleep(32000)
        val containsActions = config.containsActions("start_alarm_stopped")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

}