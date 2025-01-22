/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager.kotlin

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class EventMap<K, V> : HashMap<K, V>() {
    fun toJSONArray(): JSONArray {
        val jsonjArray = JSONArray()
        val it: Iterator<K> = keys.iterator()
        while (it.hasNext()) {
            val data = this[it.next()] as JSONObject?
            jsonjArray.put(data)
        }
        return jsonjArray
    }

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

    val isCompleteData: Boolean
        get() {
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