package com.prey.receivers

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.net.TestWebServices

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PreyBootReceiverTest {

    private lateinit var context: Context
    private val bootController = PreyBootReceiver()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testStartLock() {
        val intent = Intent()
        intent.setAction("android.intent.action.BOOT_COMPLETED")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setIntervalReport("")
        PreyConfig.getInstance(context).setExcludeReport("")
        PreyConfig.getInstance(context).setLastReportStartDate(0)
        PreyConfig.getInstance(context).setUnlockPass("osito")
        PreyConfig.getInstance(context).setOverLock(false)
        bootController.onReceive(context, intent)
        Thread.sleep(4_000)
        val overLock = PreyConfig.getInstance(context).getOverLock()
        Assert.assertTrue(overLock)
    }

    @Test
    fun testReport() {
        val intent = Intent()
        intent.setAction("android.intent.action.BOOT_COMPLETED")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setIntervalReport("")
        PreyConfig.getInstance(context).setExcludeReport("")
        PreyConfig.getInstance(context).setLastReportStartDate(0)
        PreyConfig.getInstance(context).setUnlockPass("")
        bootController.onReceive(context, intent)
        val lastReportStartDate = PreyConfig.getInstance(context).getLastReportStartDate()
        Assert.assertTrue(lastReportStartDate > 0)
    }

    @Test
    fun testClose() {
        val intent = Intent()
        intent.setAction("android.intent.action.BOOT_COMPLETED")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setIntervalReport("5")
        PreyConfig.getInstance(context).setExcludeReport("")
        PreyConfig.getInstance(context).setLastReportStartDate(0)
        bootController.onReceive(context, intent)
        val lastReportStartDate = PreyConfig.getInstance(context).getLastReportStartDate()
        Assert.assertTrue(lastReportStartDate > 0)
    }

}