/*******************************************************************************
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.lock

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.prey.PreyConfig
import com.prey.json.actions.Lock
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for Lock action JSON parameter parsing.
 * Verifies that server commands are correctly parsed into PreyConfig state.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class LockActionParsingTest {

    private lateinit var context: Context
    private lateinit var config: PreyConfig
    private lateinit var lock: Lock

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        config = PreyConfig.getPreyConfig(context)
        config.setUnlockPass("")
        config.setLockMessage("")
        config.setLock(false)
        config.setJobIdLock("")
        lock = Lock()
    }

    // =========================================================================
    // start() parameter parsing
    // =========================================================================

    @Test
    fun givenLockCommand_whenStartCalled_thenUnlockPassIsStored() {
        val params = JSONObject().apply {
            put("unlock_pass", "preyrocks")
        }
        lock.start(context, ArrayList(), params)
        assertEquals("preyrocks", config.unlockPass)
    }

    @Test
    fun givenLockCommandWithMessage_whenStartCalled_thenMessageIsStored() {
        val params = JSONObject().apply {
            put("unlock_pass", "1234")
            put("lock_message", "This device is stolen")
        }
        lock.start(context, ArrayList(), params)
        assertEquals("This device is stolen", config.lockMessage)
    }

    @Test
    fun givenLockCommandWithoutMessage_whenStartCalled_thenMessageIsEmpty() {
        val params = JSONObject().apply {
            put("unlock_pass", "1234")
        }
        lock.start(context, ArrayList(), params)
        assertEquals("", config.lockMessage)
    }

    @Test
    fun givenLockCommandWithJobId_whenStartCalled_thenJobIdIsStored() {
        val params = JSONObject().apply {
            put("unlock_pass", "1234")
            put("device_job_id", "job-456")
        }
        lock.start(context, ArrayList(), params)
        assertEquals("job-456", config.jobIdLock)
    }

    @Test
    fun givenLockCommand_whenStartCalled_thenLockFlagIsTrue() {
        val params = JSONObject().apply {
            put("unlock_pass", "test")
        }
        lock.start(context, ArrayList(), params)
        assertTrue(config.isLockSet)
    }

    // =========================================================================
    // stop() state cleanup
    // =========================================================================

    @Test
    fun givenLockedDevice_whenStopCalled_thenUnlockPassIsCleared() {
        config.setUnlockPass("secret")
        config.setLock(true)
        val params = JSONObject()
        lock.stop(context, ArrayList(), params)
        val pass = config.unlockPass
        assertTrue(pass == null || pass.isEmpty())
    }

    @Test
    fun givenLockedDevice_whenStopCalled_thenLockFlagIsFalse() {
        config.setUnlockPass("secret")
        config.setLock(true)
        val params = JSONObject()
        lock.stop(context, ArrayList(), params)
        assertFalse(config.isLockSet)
    }

    @Test
    fun givenLockedDeviceWithMessage_whenStopCalled_thenMessageIsCleared() {
        config.setUnlockPass("secret")
        config.setLockMessage("Stolen!")
        config.setLock(true)
        val params = JSONObject()
        lock.stop(context, ArrayList(), params)
        assertEquals("", config.lockMessage)
    }
}
