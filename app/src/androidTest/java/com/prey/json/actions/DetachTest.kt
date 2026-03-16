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
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetachTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var config: PreyConfig

    @Before
    fun setup() {
        config = PreyConfig.getPreyConfig(context)
        config.email = "test@preyproject.com"
        config.deviceId = "DEVICE123"
        config.apiKey = "API_KEY_VAL"
        config.protectAccount = true
        config.installationStatus = "ACTIVE"
    }

    @Test
    fun testFullDetachClearsAllSensitiveData() {
        val options = JSONObject().apply {
            put("expired", false)
        }
        Detach.execute(context, "start", options)
        assertEquals("", config.email)
        assertEquals("", config.deviceId)
        assertEquals("", config.apiKey)
        assertFalse( config.protectAccount)
        assertFalse( config.protectPrivileges)
        assertEquals("", config.installationStatus)
    }

    @Test
    fun testExpiredDetachMarksAsDeleted() {
        val options = JSONObject().apply {
            put("expired", true)
        }
        Detach.execute(context, "start", options)
        assertEquals("DEL", config.installationStatus)
        assertEquals("", config.email)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidCommandThrowsException() {
        Detach.execute(context, "invalid_cmd", JSONObject())
    }

    @Test
    fun testDetachDeviceReturnsNullOnErrorFree() {
        val result = Detach.detachDevice(
            context,
            removePermissions = false,
            removeCache = false,
            expired = false
        )
        assertNull(result)
    }

}