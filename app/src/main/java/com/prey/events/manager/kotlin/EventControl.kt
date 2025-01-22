/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager.kotlin

import com.prey.kotlin.PreyLogger
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventControl private constructor() {
    private val sdf2 = SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault())

    init {
        map = HashMap()
    }

    fun valida(json: JSONObject): Boolean {
        var state = ""
        var percentage = -1.0
        try {
            val jsonBattery = json.getJSONObject("battery_status")
            state = jsonBattery.getString("state")
            val remaining = jsonBattery.getString("percentage_remaining")
            PreyLogger.d("EVENT state:$state remaining:$remaining")
            percentage = remaining.toDouble()
        } catch (e: Exception) {
            percentage = -1.0
        }
        val nowDate = Date()
        val now = nowDate.time
        if ("discharging" == state || "stopped_charging" == state) {
            if (percentage > 0) {
                if (map!!.containsKey(state)) {
                    val time = map!![state]!!
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = time
                    if (percentage <= 15) {
                        cal.add(Calendar.MINUTE, 4)
                    } else {
                        cal.add(Calendar.MINUTE, 1)
                    }
                    val timeMore = cal.timeInMillis
                    PreyLogger.d("EVENT now        :" + now + " " + sdf2.format(Date(now)))
                    PreyLogger.d("EVENT timeMore:" + timeMore + " " + sdf2.format(Date(timeMore)))
                    if (timeMore > now) {
                        return false
                    } else {
                        map!![state] = now
                        return true
                    }
                } else {
                    map!![state] = now
                    return true
                }
            } else {
                return true
            }
        } else {
            return true
        }
    }

    companion object {

        private var map: MutableMap<String, Long>? = null


        private var INSTANCE: EventControl? = null
        fun getInstance(): EventControl {
            if (EventControl.INSTANCE == null) {
                EventControl.INSTANCE = EventControl()
            }
            return EventControl.INSTANCE!!
        }

    }
}