/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.Context
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import org.json.JSONObject

object TriggerParse {
    fun getJSONFromUrl(ctx: Context): List<TriggerDto>? {
        var json: String? = null
        try {
            json = PreyWebServices.getInstance().triggers(ctx)
            return getJSONFromTxt(ctx, json!!)
        } catch (e: Exception) {
            return null
        }
    }

    fun getJSONFromTxt(ctx: Context, jsonValue: String): List<TriggerDto>? {
        val listTrigger: MutableList<TriggerDto> = ArrayList()
        if (jsonValue != null && "" != jsonValue) {
            var json = ("{\"prey\":$jsonValue").toString() + "}"
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
                PreyLogger.e("e:" + e.message, e)
                return null
            }
        }
        return listTrigger
    }

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