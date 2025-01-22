/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.kotlin

import com.prey.kotlin.PreyLogger
import org.json.JSONException
import org.json.JSONObject

object UtilJson {
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

    fun makeMapParam(
        command: String,
        target: String,
        status: String?
    ): MutableMap<String, String?> {
        val map: MutableMap<String, String?> = HashMap()
        map["command"] = command
        map["target"] = target
        map["status"] = status
        return map
    }

    fun makeMapParam(
        command: String,
        target: String,
        status: String?,
        reason: String?
    ): MutableMap<String, String?> {
        val map = makeMapParam(command, target, status)
        if (reason != null) map["reason"] = reason
        return map
    }

    /**
     * Method get JSONObject
     *
     * @param jsonObject
     * @param key
     * @return value for key
     * @throws JSONException
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
     * Method get string
     *
     * @param jsonObject
     * @param key
     * @return value for key
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun getString(jsonObject: JSONObject?, key: String?): String? {
        var out: String? = null
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getString(key)
        }
        return out
    }

    /**
     * Method get boolean
     *
     * @param jsonObject
     * @param key
     * @return value for key
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun getBoolean(jsonObject: JSONObject?, key: String?): Boolean {
        var out = false
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getBoolean(key)
        }
        return out
    }

    /**
     * Method get int
     *
     * @param jsonObject
     * @param key
     * @return value for key
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun getInt(jsonObject: JSONObject?, key: String?): Int {
        var out = 0
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getInt(key)
        }
        return out
    }
}