/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.parser

import android.content.Context
import com.prey.json.CommandTarget
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class JsonCommandDispatcherTest {

    private lateinit var context: Context

    companion object {
        var executedCommand: String? = null
        var executedOptions: JSONObject? = null
    }

    class FakeTarget : CommandTarget {
        override fun execute(context: Context, command: String, options: JSONObject) {
            executedCommand = command
            executedOptions = options
        }
    }

    @Before
    fun setup() {
        context = mock(Context::class.java)
        executedCommand = null
        executedOptions = null
    }

    private fun createDispatcherWithFakeTarget(): JsonCommandDispatcher {
        val map = JsonCommandDispatcher.allowedTargets.toMutableMap()
        map["Test"] = FakeTarget::class
        val field = JsonCommandDispatcher::class.java.getDeclaredField("allowedTargets")
        field.isAccessible = true
        field.set(JsonCommandDispatcher, map)
        return JsonCommandDispatcher
    }

    @Test
    fun dispatchJson_should_execute_valid_command() {
        createDispatcherWithFakeTarget()
        val json = JSONObject().apply {
            put("target", "Test")
            put("command", "run")
            put("options", JSONObject().apply {
                put("speed", 10)
            })
        }
        JsonCommandDispatcher.dispatchJson(context, json)
        assertEquals("run", executedCommand)
        assertEquals(10, executedOptions?.getInt("speed"))
    }

    @Test
    fun dispatchJson_should_handle_empty_options() {
        createDispatcherWithFakeTarget()
        val json = JSONObject().apply {
            put("target", "Test")
            put("command", "start")
        }
        JsonCommandDispatcher.dispatchJson(context, json)
        assertEquals("start", executedCommand)
    }

    @Test
    fun dispatchJson_should_throw_when_target_not_allowed() {
        val json = JSONObject().apply {
            put("target", "Unknown")
            put("command", "run")
        }
        assertThrows(IllegalArgumentException::class.java) {
            JsonCommandDispatcher.dispatchJson(context, json)
        }
    }

    @Test
    fun dispatchArray_should_process_multiple_commands() {
        createDispatcherWithFakeTarget()
        val array = JSONArray().apply {
            put(JSONObject().apply {
                put("target", "Test")
                put("command", "one")
            })
            put(JSONObject().apply {
                put("target", "Test")
                put("command", "two")
            })
        }
        JsonCommandDispatcher.dispatchArray(context, array)
        assertEquals("two", executedCommand)
    }

    @Test
    fun dispatchJson_should_fail_when_target_missing() {
        val json = JSONObject().apply {
            put("command", "run")
        }
        assertThrows(Exception::class.java) {
            JsonCommandDispatcher.dispatchJson(context, json)
        }
    }

}