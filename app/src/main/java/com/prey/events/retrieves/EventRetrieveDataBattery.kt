/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.prey.actions.battery.BatteryInformation
import com.prey.events.manager.EventManager
import com.prey.PreyLogger
import org.json.JSONException
import org.json.JSONObject

/**
 * EventRetrieveDataBattery is a class responsible for retrieving battery information.
 */
class EventRetrieveDataBattery {
    private var manager: EventManager? = null

    /**
     * Executes the battery information retrieval.
     *
     * @param context The application context.
     * @param manager The EventManager instance.
     */
    fun execute(context: Context, manager: EventManager?) {
        this.manager = manager
        try {
            context.applicationContext.registerReceiver(
                this.mBatInfoReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    /**
     * The BroadcastReceiver for the battery information.
     */
    private val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        /**
         * Called when the battery information is received.
         *
         * @param context The context.
         * @param intent The intent.
         */
        override fun onReceive(context: Context, intent: Intent) {
            try {
                // Get the battery information.s
                val battery = BatteryInformation().makeBattery(intent)
                context.unregisterReceiver(this)
                if (battery != null) {
                    val state = if (battery.isCharging()) "charging" else "discharging"
                    val remaining = battery.getLevel().toString()
                    val batteryJSon = JSONObject()
                    try {
                        // Create a JSONObject for the battery information.
                        val batteryElementJSon = JSONObject()
                        batteryElementJSon.put("state", state)
                        batteryElementJSon.put("percentage_remaining", remaining)
                        batteryJSon.put("battery_status", batteryElementJSon)
                    } catch (e: JSONException) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    PreyLogger.d("battery: state[$state] remaining[$remaining]")
                    manager!!.receivesData(EventManager.BATTERY, batteryJSon)
                }
            } catch (e: Exception) {
                val batteryJSon = JSONObject()
                manager!!.receivesData(EventManager.BATTERY, batteryJSon)
            }
        }
    }
}