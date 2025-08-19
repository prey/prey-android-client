package com.prey.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig

import org.junit.Before
import org.junit.Test

class CheckLockServiceTest {

    private lateinit var context: Context
    private val checkLockService= CheckLockService()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
        //context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    fun test_onNewToken() {
        PreyConfig.getInstance(context).setUnlockPass("")
        checkLockService.checkLock(context)
    }

}