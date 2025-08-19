package com.prey.json.actions

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.actions.observer.ActionResult
import com.prey.net.PreyWebServices
import com.prey.net.TestWebServices

import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class TreeTest {

    private lateinit var context: Context
    private lateinit var parameters: JSONObject
    private lateinit var actionResults: List<ActionResult?>
    private lateinit var preyWebServices: PreyWebServices
    private lateinit var tree: Tree

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
        val apikey = "A123"
        val deviceId = "d456"
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setApiKey(apikey)
        PreyConfig.getInstance(context).setDeviceId(deviceId)
        tree = Tree()
        parameters = JSONObject()
    }

    @Test
    fun testGetMethodWithValidParameters() {
        // Arrange
        val messageId = "messageId"
        val jobId = "jobId"
        val depth = 2
        val path = "/path"
        parameters.put(PreyConfig.MESSAGE_ID, messageId)
        parameters.put(PreyConfig.JOB_ID, jobId)
        parameters.put("depth", depth.toString())
        parameters.put("path", path)
        // Act
        tree.get(context, actionResults, parameters)
    }

    @Test
    fun testGetMethodWithInvalidDepthParameter() {
        // Arrange
        val messageId = "messageId"
        val jobId = "jobId"
        val path = "/path"
        parameters.put(PreyConfig.MESSAGE_ID, messageId)
        parameters.put(PreyConfig.JOB_ID, jobId)
        parameters.put("path", path)
        // Act and Assert
        try {
            tree.get(context, actionResults, parameters)
            assert(false)
        } catch (e: Exception) {
            assert(true)
        }
    }

    @Test
    fun testGetMethodWithInvalidPathParameter() {
        // Arrange
        val messageId = "messageId"
        val jobId = "jobId"
        val depth = 2
        parameters.put(PreyConfig.MESSAGE_ID, messageId)
        parameters.put(PreyConfig.JOB_ID, jobId)
        parameters.put("depth", depth.toString())
        // Act and Assert
        try {
            tree.get(context, actionResults, parameters)
            assert(false)
        } catch (e: Exception) {
            assert(true)
        }
    }

    @Test
    fun testGetMethodWithException() {
        // Arrange
        val messageId = "messageId"
        val jobId = "jobId"
        val depth = 2
        val path = "/path"
        parameters.put(PreyConfig.MESSAGE_ID, messageId)
        parameters.put(PreyConfig.JOB_ID, jobId)
        parameters.put("depth", depth.toString())
        parameters.put("path", path)
        // Act and Assert
        try {
            tree.get(context, actionResults, parameters)
            assert(false)
        } catch (e: Exception) {
            assert(true)
        }
    }

}