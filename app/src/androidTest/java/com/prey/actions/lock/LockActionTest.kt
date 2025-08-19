package com.prey.actions.lock

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.TestWebServices

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LockActionTest {

    private lateinit var lockAction: LockAction
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PreyConfig.getInstance(context)
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        lockAction = LockAction()
    }

    @Test
    fun test_upload_tree_directory_exists() {
        val messageId = "123"
        val jobId = "456"
        val unlock = "osito"
        val reason = ""
        lockAction.start(context, messageId, jobId, reason)
        val webServices = PreyConfig.getInstance(context).getWebServices() as TestWebServices
        val parameters = webServices.getNotificationParameters()
        // Verify
        Assert.assertNotNull(parameters)
        if (!parameters.isNullOrEmpty()) {
            PreyLogger.d("parameters:${parameters.toString()}")
            val correlationId = parameters.get("correlationId")
            val command = parameters.get("command")
            val target = parameters.get("target")
            val status = parameters.get("status")
            Assert.assertEquals(messageId, correlationId)
            Assert.assertEquals(command, "get")
            Assert.assertEquals(target, "tree")
            Assert.assertEquals(status, "started")
        }
    }

}