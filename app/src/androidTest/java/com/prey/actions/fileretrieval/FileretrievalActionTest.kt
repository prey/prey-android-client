package com.prey.actions.fileretrieval

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
class FileretrievalActionTest {

    private lateinit var fileretrievalAction: FileretrievalAction
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PreyConfig.getInstance(context)
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        fileretrievalAction = FileretrievalAction()
    }

    @Test
    fun test_upload_file_exits() {
        val messageId = "123"
        val jobId = "456"
        val path = "DCIM/oso.png"
        val fileId = "456"
        fileretrievalAction.start(context, messageId, jobId, path, fileId)
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
            Assert.assertEquals(command, "start")
            Assert.assertEquals(target, "fileretrieval")
            Assert.assertEquals(status, "stopped")
        }
    }

    @Test
    fun test_upload_file_not_exits() {
        val messageId = "123"
        val jobId = "456"
        val path = "DCIM/oso2.png"
        val fileId = "456"
        fileretrievalAction.start(context, messageId, jobId, path, fileId)
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
            Assert.assertEquals(command, "start")
            Assert.assertEquals(target, "fileretrieval")
            Assert.assertEquals(status, "failed")
        }
    }

}