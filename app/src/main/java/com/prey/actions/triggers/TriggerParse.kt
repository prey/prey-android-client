/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.Context
import com.prey.PreyConfig

import com.prey.PreyLogger
import com.prey.net.PreyWebServices

import org.json.JSONObject

/**
 * TriggerParse is an object that provides methods for parsing trigger data from JSON.
 */
object TriggerParse {

    /**
     * Retrieves trigger data from a URL and parses it into a list of TriggerDto objects.
     *
     * @param context The application context.
     * @return A list of TriggerDto objects, or null if an error occurs.
     */
    fun getJSONFromUrl(context: Context): List<TriggerDto>? {
        var json: String? = null
        try {
            json = PreyConfig.getInstance(context).getWebServices().triggers(context)
            if (json != null) {
                return getJSONFromTxt(context, json)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)

        }
        return null
    }

    /**
     * Parses a JSON string into a list of TriggerDto objects.
     *
     * @param context The application context.
     * @param jsonValue The JSON string to parse.
     * @return A list of TriggerDto objects, or null if an error occurs.
     */
    fun getJSONFromTxt(context: Context, jsonValue: String): List<TriggerDto>? {
        val listTrigger: MutableList<TriggerDto> = ArrayList()
        if (jsonValue != null && "" != jsonValue) {
            var json = "{\"prey\":${jsonValue}}"
            PreyLogger.d(json)
            try {
                val jsnobject = JSONObject(json)
                val jsonArray = jsnobject.getJSONArray("prey")
                for (i in 0 until jsonArray.length()) {
                    val jsonCommand = jsonArray[i].toString()
                    val explrObject = JSONObject(jsonCommand)
                    val trigger = TriggerDto()
                    try {
                        trigger.setId(explrObject.getInt("id"))
                    } catch (e: java.lang.Exception) {
                        trigger.setId(101)
                    }
                    try {
                        trigger.setName(explrObject.getString("name"))
                    } catch (e: java.lang.Exception) {
                        trigger.setName("ups")
                    }
                    trigger.setEvents(explrObject.getString("automation_events"))
                    trigger.setActions(explrObject.getString("automation_actions"))
                    listTrigger.add(trigger)
                }
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error: ${e.message}", e)
                return null
            }
        }
        return listTrigger
    }

    /**
     * Parses a JSON string into a list of TriggerEventDto objects.
     *
     * @param events The JSON string to parse.
     * @return A list of TriggerEventDto objects, or null if an error occurs.
     */
    fun TriggerEvents(events: String): List<TriggerEventDto>? {
        var events = events
        events = "{\"prey\":$events}"
        val listTrigger: MutableList<TriggerEventDto> = ArrayList()
        PreyLogger.d(events)
        try {
            val jsnobject = JSONObject(events)
            val jsonArray = jsnobject.getJSONArray("prey")
            for (i in 0 until jsonArray.length()) {
                val jsonCommand = jsonArray[i].toString()
                val explrObject = JSONObject(jsonCommand)
                val trigger = TriggerEventDto()
                trigger.setType(explrObject.getString("type"))
                trigger.setInfo(explrObject.getString("info"))
                listTrigger.add(trigger)
            }
        } catch (e: Exception) {
            return null
        }
        return listTrigger
    }

    /**
     * Parses a JSON string into a list of TriggerActionDto objects.
     *
     * @param actions The JSON string to parse.
     * @return A list of TriggerActionDto objects, or null if an error occurs.
     */
    fun TriggerActions(actions: String): List<TriggerActionDto>? {
        var actions = actions
        actions = "{\"prey\":$actions}"
        val listTrigger: MutableList<TriggerActionDto> = ArrayList()
        PreyLogger.d(actions)
        try {
            val jsnobject = JSONObject(actions)
            val jsonArray = jsnobject.getJSONArray("prey")
            for (i in 0 until jsonArray.length()) {
                val jsonCommand = jsonArray[i].toString()
                val explrObject = JSONObject(jsonCommand)
                val trigger = TriggerActionDto()
                trigger.setDelay(explrObject.getInt("delay"))
                trigger.setAction(explrObject.getString("action"))
                listTrigger.add(trigger)
            }
        } catch (e: Exception) {
            return null
        }
        return listTrigger
    }

}