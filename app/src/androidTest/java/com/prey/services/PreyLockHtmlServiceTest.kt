package com.prey.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig

import org.junit.Before
import org.junit.Test

class PreyLockHtmlServiceTest {

    private lateinit var context: Context
    private val preyLockHtmlService = PreyLockHtmlService()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_lock() {
        PreyConfig.getInstance(context).setUnlockPass("osito")
        PreyConfig.getInstance(context).setLockMessage("")
        PreyConfig.getInstance(context).setOverLock(false)
        preyLockHtmlService.onStart(context)
        val overLock = PreyConfig.getInstance(context).getOverLock()
        assert(overLock)
    }

}