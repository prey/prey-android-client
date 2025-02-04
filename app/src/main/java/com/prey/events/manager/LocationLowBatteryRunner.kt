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
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LocationLowBatteryRunner(ctx: Context?) : Runnable {
    private var ctx: Context? = null

    override fun run() {
        PreyLogger.d("EVENT LocationLowBatteryRunner")
        try {
            val jsonString =
                "[ {\"command\": \"get\",\"target\": \"location_low_battery\",\"options\": {}}]"
            val jsonObjectList: List<JSONObject>? =
                JSONParser().getJSONFromTxt(ctx!!, jsonString.toString())
            if (jsonObjectList != null && jsonObjectList.size > 0) {
                ActionsController.getInstance().runActionJson(ctx!!, jsonObjectList)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }


    fun isValid(ctx: Context): Boolean {
        try {
            val cal: Calendar = Calendar.getInstance()
            cal.setTime(Date())
            cal.add(Calendar.HOUR, -3)
            val leastSixHours: Long = cal.getTimeInMillis()
            val locationLowBatteryDate: Long =
                PreyConfig.getInstance(ctx).getLocationLowBatteryDate()
            PreyLogger.d(
                "EVENT locationLowBatteryDate :" + locationLowBatteryDate + " " + sdf.format(
                    Date(locationLowBatteryDate)
                )
            )
            PreyLogger.d(
                "EVENT leastSixHours   :" + leastSixHours + " " + sdf.format(
                    Date(
                        leastSixHours
                    )
                )
            )
            PreyLogger.d("EVENT diff:" + (leastSixHours - locationLowBatteryDate))
            if (locationLowBatteryDate == 0L || leastSixHours > locationLowBatteryDate) {
                val now: Long = Date().getTime()
                PreyConfig.getInstance(ctx).setLocationLowBatteryDate(now)
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    companion object {
        private val sdf: SimpleDateFormat =
            SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault())
        private var INSTANCE: LocationLowBatteryRunner? = null

        fun getInstance(context: Context): LocationLowBatteryRunner {
            if (INSTANCE == null) {
                INSTANCE = LocationLowBatteryRunner(context)
            }
            return INSTANCE!!
        }

    }
}