/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json

import com.prey.PreyLogger
import org.json.JSONException
import org.json.JSONObject

/**
 * Utility object for working with JSON data.
 */
object UtilJson {

    /**
     * Creates a JSON response with the given command, target, and status.
     *
     * @param command The command to include in the response.
     * @param target The target to include in the response.
     * @param status The status to include in the response.
     * @return A JSONObject representing the response.
     */
    fun makeJsonResponse(command: String?, target: String?, status: String?): JSONObject {
        val json = JSONObject()
        try {
            json.put("command", command)
            json.put("target", target)
            json.put("status", status)
        } catch (e: JSONException) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return json
    }

    /**
     * Creates a JSON response with the given command, target, status, and reason.
     *
     * @param command The command to include in the response.
     * @param target The target to include in the response.
     * @param status The status to include in the response.
     * @param reason The reason to include in the response.
     * @return A JSONObject representing the response.
     */
    fun makeJsonResponse(
        command: String?,
        target: String?,
        status: String?,
        reason: String?
    ): JSONObject {
        val json = makeJsonResponse(command, target, status)
        try {
            json.put("reason", reason)
        } catch (e: JSONException) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return json
    }

    /**
     * Creates a map of parameters with the given command, target, and status.
     *
     * @param command The command to include in the map.
     * @param target The target to include in the map.
     * @param status The status to include in the map.
     * @return A MutableMap of parameters.
     */
    fun makeMapParam(
        command: String,
        target: String,
        status: String?
    ): MutableMap<String, String?> {
        val paramMap = mutableMapOf(
            "command" to command,
            "target" to target,
            "status" to status
        )
        return paramMap
    }

    /**
     * Creates a map of parameters with the given command, target, status, and reason.
     *
     * @param command The command to include in the map.
     * @param target The target to include in the map.
     * @param status The status to include in the map.
     * @param reason The reason to include in the map.
     * @return A MutableMap of parameters.
     */
    fun makeMapParam(
        command: String,
        target: String,
        status: String?,
        reason: String?
    ): MutableMap<String, String?> {
        val paramMap = makeMapParam(command, target, status)
        reason?.let { paramMap["reason"] = it }
        return paramMap
    }

    /**
     * Retrieves a JSONObject from a given JSONObject based on a specified key.
     *
     * @param jsonObject The JSONObject to retrieve the value from.
     * @param key The key to look up in the JSONObject.
     * @return The JSONObject associated with the key, or null if not found.
     * @throws JSONException If the key is not found or the value is not a JSONObject.
     */
    @Throws(JSONException::class)
    fun getJSONObject(jsonObject: JSONObject?, key: String?): JSONObject? {
        var out: JSONObject? = null
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getJSONObject(key)
        }
        return out
    }

    /**
     * Retrieves a string value from a given JSONObject based on a specified key.
     *
     * @param jsonObject The JSONObject to retrieve the value from.
     * @param key The key to look up in the JSONObject.
     * @return The string value associated with the key, or null if not found.
     * @throws JSONException If the key is not found or the value is not a string.
     */
    @Throws(JSONException::class)
    fun getStringValue(jsonObject: JSONObject?, key: String?): String? {
        var out: String? = null
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getString(key)
        }
        return out
    }

    /**
     * Retrieves a boolean value from a given JSONObject based on a specified key.
     *
     * @param jsonObject The JSONObject to retrieve the value from.
     * @param key The key to look up in the JSONObject.
     * @return The boolean value associated with the key, or false if not found.
     * @throws JSONException If the key is not found or the value is not a boolean.
     */
    @Throws(JSONException::class)
    fun getBooleanValue(jsonObject: JSONObject?, key: String?): Boolean {
        var out = false
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getBoolean(key)
        }
        return out
    }

    /**
     * Retrieves an integer value from a given JSONObject based on a specified key.
     *
     * @param jsonObject The JSONObject to retrieve the value from.
     * @param key The key to look up in the JSONObject.
     * @return The integer value associated with the key, or 0 if not found.
     * @throws JSONException If the key is not found or the value is not an integer.
     */
    @Throws(JSONException::class)
    fun getIntValue(jsonObject: JSONObject?, key: String?): Int {
        var out = 0
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getInt(key)
        }
        return out
    }
}