package com.prey.actions.observer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class ActionsControllerTest {

    private lateinit var context: Context
    private lateinit var actionsController: ActionsController

    private lateinit var jsonObjects: List<JSONObject>

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
        actionsController = ActionsController()
    }

    @Test
    fun testRunActionJson() {
        // Given
        val jsonObject = JSONObject()
        jsonObject.put("cmd", JSONObject())
        jsonObject.getJSONObject("cmd").put("target", "target")
        jsonObject.getJSONObject("cmd").put("command", "command")
        jsonObjects = listOf(jsonObject)

        // When
        val result = actionsController.runActionJson(context, jsonObjects)

        // Then
        assert(result != null)
    }

    @Test
    fun testRunActionJson_EmptyJsonObjects() {
        // Given
        jsonObjects = emptyList()

        // When
        val result = actionsController.runActionJson(context, jsonObjects)

        // Then
        assert(result != null)
    }

    @Test
    fun testRunActionJson_InvalidJsonObjects() {
        // Given
        val jsonObject = JSONObject()
        jsonObject.put("cmd", "invalid")
        jsonObjects = listOf(jsonObject)

        // When
        val result = actionsController.runActionJson(context, jsonObjects)

        // Then
        assert(result == null)
    }

    @Test
    fun testRunActionJson_Exception() {
        // Given
        val jsonObject = JSONObject()
        jsonObject.put("cmd", JSONObject())
        jsonObject.getJSONObject("cmd").put("target", "target")
        jsonObject.getJSONObject("cmd").put("command", "command")
        jsonObjects = listOf(jsonObject)

        // When
        val result = actionsController.runActionJson(context, jsonObjects)

        // Then
        assert(result != null)
    }

}