package com.prey.actions.wipe

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.prey.PreyConfig
import com.prey.net.TestWebServices

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WipeTest {

    private lateinit var wipeAction: WipeAction
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        wipeAction = WipeAction()
    }

    @Test
    fun test_wipe() {
        val messageId = "123"
        val jobId = "456"
        val wipe = true
        val deleteSD = true
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setWipe(TestWipe())
        wipeAction.start(context, messageId, jobId, wipe, deleteSD)
        val webServices = PreyConfig.getInstance(context).getWebServices() as TestWebServices
        val parameters = webServices.getNotificationParameters()
        // Verify
        Assert.assertNotNull(parameters)
        if (!parameters.isNullOrEmpty()) {
            val correlationId = parameters.get("correlationId")
            val command = parameters.get("command")
            val target = parameters.get("target")
            val status = parameters.get("status")
            Assert.assertEquals(messageId, correlationId)
            Assert.assertEquals(command, "start")
            Assert.assertEquals(target, "wipe")
            Assert.assertEquals(status, "stopped")
        }
    }

}