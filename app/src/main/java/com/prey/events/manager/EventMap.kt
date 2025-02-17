/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Checks if all values in the EventMap are non-null.
 *
 * @return True if all values are non-null, false otherwise.
 */
class EventMap<K, V> : HashMap<K, V>() {
    /**
     * Converts the EventMap to a JSONArray.
     *
     * @return JSONArray representation of the EventMap.
     */
    fun toJSONArray(): JSONArray {
        val jsonjArray = JSONArray()
        val it: Iterator<K> = keys.iterator()
        while (it.hasNext()) {
            val data = this[it.next()] as JSONObject?
            jsonjArray.put(data)
        }
        return jsonjArray
    }

    /**
     * Converts the EventMap to a JSONObject.
     *
     * @return JSONObject representation of the EventMap.
     */
    fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        val it: Iterator<K> = keys.iterator()
        while (it.hasNext()) {
            val data = this[it.next()] as JSONObject?
            val ite2: Iterator<*> = data!!.keys()
            while (ite2.hasNext()) {
                val name = ite2.next() as String
                try {
                    jsonObject.put(name, data[name])
                } catch (e: JSONException) {
                }
            }
        }
        return jsonObject
    }

    /**
     * Checks if all values in the EventMap are non-null.
     *
     * @return True if all values are non-null, false otherwise.
     */
    fun isCompleteData(): Boolean {
        var isCompleteData = true
        val it: Iterator<K> = keys.iterator()
        while (it.hasNext()) {
            val data = this[it.next()] as JSONObject?
            if (data == null) {
                isCompleteData = false
                break
            }
        }
        return isCompleteData
    }
}