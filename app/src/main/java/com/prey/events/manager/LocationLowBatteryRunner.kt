/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager

import android.content.Context

import com.prey.actions.observer.ActionsController
import com.prey.json.parser.JSONParser
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.backwardcompatibility.FroyoSupport

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * This class represents a runner for location low battery events.
 * It implements the Runnable interface and provides a method to check if the event is valid.
 */
class LocationLowBatteryRunner(var context: Context) : Runnable {

    /**
     * This method is called when the runner is executed.
     * It sends a request to get the location low battery data and runs the corresponding action.
     */
    override fun run() {
        PreyLogger.d("EVENT LocationLowBatteryRunner")
        try {
            val jsonString =
                "[ {\"command\": \"get\",\"target\": \"location_low_battery\",\"options\": {}}]"
            val jsonObjectList: List<JSONObject>? =
                JSONParser().getJSONFromTxt(context, jsonString.toString())
            // If the list is not empty, run the corresponding action
            if (jsonObjectList != null && jsonObjectList.size > 0) {
                ActionsController.getInstance().runActionJson(context, jsonObjectList)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    /**
     * This method checks if the location low battery event is valid.
     * It returns true if the event is valid, false otherwise.
     */
    fun isValid(): Boolean {
        try {
            val cal: Calendar = Calendar.getInstance()
            cal.setTime(Date())
            cal.add(Calendar.HOUR, -3)
            val leastSixHours: Long = cal.getTimeInMillis()
            val locationLowBatteryDate: Long =
                PreyConfig.getInstance(context).getLocationLowBatteryDate()
            PreyLogger.d(
                "EVENT locationLowBatteryDate :${locationLowBatteryDate} ${
                    dateFormat.format(
                        Date(locationLowBatteryDate)
                    )
                }"
            )
            PreyLogger.d(
                "EVENT leastSixHours   :${leastSixHours} ${
                    dateFormat.format(
                        Date(
                            leastSixHours
                        )
                    )
                }"
            )
            PreyLogger.d("EVENT diff:${(leastSixHours - locationLowBatteryDate)}")
            if (locationLowBatteryDate == 0L || leastSixHours > locationLowBatteryDate) {
                val now: Long = Date().getTime()
                PreyConfig.getInstance(context).setLocationLowBatteryDate(now)
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    companion object {
        private val dateFormat: SimpleDateFormat =
            SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault())
        private var instance: LocationLowBatteryRunner? = null
        fun getInstance(context: Context): LocationLowBatteryRunner =
            instance ?: LocationLowBatteryRunner(context).also { instance = it }
    }
}