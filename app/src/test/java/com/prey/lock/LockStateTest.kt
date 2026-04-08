/*******************************************************************************
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.lock

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.prey.PreyConfig
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for lock state management in PreyConfig.
 * Verifies that lock/unlock operations correctly update SharedPreferences.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class LockStateTest {

    private lateinit var context: Context
    private lateinit var config: PreyConfig

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        config = PreyConfig.getPreyConfig(context)
        config.setUnlockPass("")
        config.setLockMessage("")
        config.setLock(false)
        config.setJobIdLock("")
    }

    // =========================================================================
    // Unlock password
    // =========================================================================

    @Test
    fun givenNoLock_whenUnlockPassSet_thenCanBeRetrieved() {
        config.setUnlockPass("test1234")
        assertEquals("test1234", config.unlockPass)
    }

    @Test
    fun givenNoLock_whenUnlockPassEmpty_thenReturnsEmpty() {
        assertEquals("", config.unlockPass)
    }

    @Test
    fun givenLockedDevice_whenUnlockPassDeleted_thenReturnsEmpty() {
        config.setUnlockPass("secret")
        config.deleteUnlockPass()
        val pass = config.unlockPass
        assertTrue(pass == null || pass.isEmpty())
    }

    @Test
    fun givenLockedDevice_whenPasswordUpdated_thenNewValueReturned() {
        config.setUnlockPass("old_pass")
        config.setUnlockPass("new_pass")
        assertEquals("new_pass", config.unlockPass)
    }

    // =========================================================================
    // Lock message
    // =========================================================================

    @Test
    fun givenLockMessage_whenSet_thenCanBeRetrieved() {
        config.setLockMessage("Device stolen")
        assertEquals("Device stolen", config.lockMessage)
    }

    @Test
    fun givenNoMessage_whenRetrieved_thenReturnsEmpty() {
        assertEquals("", config.lockMessage)
    }

    // =========================================================================
    // Lock flag
    // =========================================================================

    @Test
    fun givenUnlockedDevice_whenLockSet_thenIsLocked() {
        config.setLock(true)
        assertTrue(config.isLockSet)
    }

    @Test
    fun givenLockedDevice_whenUnlocked_thenIsNotLocked() {
        config.setLock(true)
        config.setLock(false)
        assertFalse(config.isLockSet)
    }

    // =========================================================================
    // Job ID
    // =========================================================================

    @Test
    fun givenJobId_whenSet_thenCanBeRetrieved() {
        config.setJobIdLock("job-123")
        assertEquals("job-123", config.jobIdLock)
    }

    @Test
    fun givenJobId_whenCleared_thenReturnsEmpty() {
        config.setJobIdLock("job-123")
        config.setJobIdLock("")
        assertEquals("", config.jobIdLock)
    }
}
