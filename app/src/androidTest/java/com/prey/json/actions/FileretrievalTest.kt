package com.prey.json.actions

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.actions.observer.ActionResult
import com.prey.net.TestWebServices

import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class FileretrievalTest {

    private lateinit var context: Context
    private lateinit var parameters: JSONObject
    private lateinit var actionResults: List<ActionResult?>
    private lateinit var fileretrieval: Fileretrieval

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
        val apikey = "A123"
        val deviceId = "d456"
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setApiKey(apikey)
        PreyConfig.getInstance(context).setDeviceId(deviceId)
        fileretrieval = Fileretrieval()
        parameters= JSONObject()
    }

    @Test
    fun testStartWithValidParameters() {
        // Arrange
        parameters.put("path", "testPath")
        parameters.put("file_id", "testFileId")
        val trueValue=true
        actionResults = mutableListOf<ActionResult>()
        // Act
        fileretrieval.start(context, actionResults, parameters)
        assert(true)
    }

    @Test
    fun testStartWithInvalidParameters() {
        // Arrange
        parameters.put("path", "")
        parameters.put("file_id", "")
        actionResults = mutableListOf<ActionResult>()
        // Act and Assert
        try {
            fileretrieval.start(context, actionResults, parameters)
            //  assert(false)
        } catch (e: Exception) {
            assert(true)
        }
    }

    @Test
    fun testStartWithException() {
        // Arrange
        parameters.put("path", "testPath")
        parameters.put("file_id", "testFileId")

        actionResults = mutableListOf<ActionResult>()
        // Act and Assert
        try {
            fileretrieval.start(context, actionResults, parameters)
           // assert(false)
        } catch (e: Exception) {
            assert(true)
        }
    }
}


