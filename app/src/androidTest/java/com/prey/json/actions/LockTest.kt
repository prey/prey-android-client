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
import com.prey.PreyLogger
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LockTest {
    private var context: Context? = null
    private lateinit var config: PreyConfig


    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        config = PreyConfig.getPreyConfig(context)
    }

    @Test
    fun test_lock_start_saves_options_correctly() = runBlocking {
        config.deleteActions();
        val options = JSONObject().apply {
            put(PreyConfig.UNLOCK_PASS, "1234")
            put(PreyConfig.LOCK_MESSAGE, "Locked by Prey")
        }
        Lock.start(context!!, options)
        Assert.assertTrue(config.unlockPass == "1234")
        Assert.assertTrue(config.lockMessage == "Locked by Prey")
        Assert.assertTrue(config.isLockSet)
        Thread.sleep(1000)
        val containsActions = config.containsActions("start_lock_started")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

    @Test
    fun test_lock_stop_saves_options_correctly() = runBlocking {
        config.deleteActions();
        val options = JSONObject().apply {
            put(PreyConfig.UNLOCK_PASS, "1234")
            put(PreyConfig.LOCK_MESSAGE, "Locked by Prey")
        }
        Lock.start(context!!, options)
        Thread.sleep(2000)
        PreyLogger.d("unlockPass:${config.unlockPass}")
        PreyLogger.d("lockMessage:${config.lockMessage}")
        PreyLogger.d("isLockSet:${config.isLockSet}")
        Lock.stop(context!!, options)
        Thread.sleep(3000)
        PreyLogger.d("unlockPass:${config.unlockPass}")
        PreyLogger.d("lockMessage:${config.lockMessage}")
        PreyLogger.d("isLockSet:${config.isLockSet}")
        Assert.assertNull(config.unlockPass)
        Assert.assertEquals(config.lockMessage , "")
        Assert.assertFalse(config.isLockSet)
        val containsActions = config.containsActions("stop_lock_stopped")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

}