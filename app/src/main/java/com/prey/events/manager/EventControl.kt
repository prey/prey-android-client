/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager

import android.content.Context
import com.prey.PreyLogger

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * EventControl is a singleton class responsible for validating events based on battery status.
 */
class EventControl private constructor() {
    private val dateFormat = SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault())

    init {
        // Initialize the map to store battery state timestamps
        map = HashMap()
    }

    /**
     * Validates an event based on the provided JSON object containing battery status information.
     *
     * @param json JSONObject containing battery status information
     * @return True if the event is valid, false otherwise
     */
    fun isValid(json: JSONObject): Boolean {
        var state = ""
        var percentage: Double
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
        // Check if battery is discharging or stopped charging
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
                    PreyLogger.d("EVENT now     :${now} ${dateFormat.format(Date(now))}")
                    PreyLogger.d("EVENT timeMore:${timeMore} ${dateFormat.format(Date(timeMore))}"  )
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
        private var instance: EventControl? = null
        fun getInstance(): EventControl =
            instance ?: EventControl().also { instance = it }
    }
}