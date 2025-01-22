/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves.kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.prey.actions.battery.kotlin.BatteryInformation
import com.prey.events.manager.kotlin.EventManager
import com.prey.kotlin.PreyLogger
import org.json.JSONException
import org.json.JSONObject

class EventRetrieveDataBattery {
    private var manager: EventManager? = null

    fun execute(context: Context, manager: EventManager?) {
        this.manager = manager
        try {
            context.applicationContext.registerReceiver(
                this.mBatInfoReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    private val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            try {
                val battery = BatteryInformation().makeBattery(intent)
                ctx.unregisterReceiver(this)
                if (battery != null) {
                    val state = if (battery.isCharging()) "charging" else "discharging"
                    val remaining = battery.getLevel().toString()
                    val batteryJSon = JSONObject()
                    try {
                        val batteryElementJSon = JSONObject()
                        batteryElementJSon.put("state", state)
                        batteryElementJSon.put("percentage_remaining", remaining)
                        batteryJSon.put("battery_status", batteryElementJSon)
                    } catch (e: JSONException) {
                        PreyLogger.e("Error put:" + e.message, e)
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