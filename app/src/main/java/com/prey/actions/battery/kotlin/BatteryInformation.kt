/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.battery.kotlin

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.prey.actions.kotlin.HttpDataService
import com.prey.kotlin.PreyLogger


class BatteryInformation {
    var battery: Battery? = null

    private val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.ECLAIR)
        override fun onReceive(arg0: Context, intent: Intent) {
            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
            val iconSmall = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0)
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val present = intent.extras!!.getBoolean(BatteryManager.EXTRA_PRESENT)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
            val technology = intent.extras!!.getString(BatteryManager.EXTRA_TECHNOLOGY)
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            battery = Battery()
            battery!!.setHealth(health);
            battery!!.setIconSmall(iconSmall);
            battery!!.setLevel(level);
            battery!!.setPlugged(plugged);
            battery!!.setPresent(present);
            battery!!.setScale(scale);
            battery!!.setStatus(status);
            battery!!.setTechnology(technology);
            battery!!.setTemperature(temperature);
            battery!!.setVoltage(voltage);
            battery!!.setCharging(charging);
            PreyLogger.d("voltage:$voltage status:$status technology:$technology temperature:$voltage")
            arg0.unregisterReceiver(this)
        }
    }

    fun getInformation(context: Context): HttpDataService? {
        battery = null
        context.applicationContext.registerReceiver(
            this.mBatInfoReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        try {
            var i = 0
            while (battery == null && i < 10) {
                Thread.sleep(1000)
                i++
            }
        } catch (e: InterruptedException) {
            PreyLogger.d("Error, causa:" + e.message)
        }
        var data: HttpDataService? = null
        if (battery != null) {
            data = HttpDataService("battery_status")
            val parametersMap = HashMap<String, String>()
            parametersMap["state"] = if (battery!!.isCharging()) "charging" else "discharging"
            parametersMap["remaining"] = battery!!.getLevel().toString()
            data.getDataList()!!.plus(parametersMap)
            data.setList(true)
        }
        return data
    }

    private val batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.ECLAIR)
        override fun onReceive(context: Context, intent: Intent) {

            battery = Battery()
            battery!!.setHealth (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0))
            battery!!.setIconSmall ( intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0))
            battery!!.setLevel (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0))
            battery!!.setPlugged (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0))
            battery!!.setPresent (intent.extras!!.getBoolean(BatteryManager.EXTRA_PRESENT))
            battery!!.setScale(intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0))
            battery!!.setStatus ( intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0))
            battery!!.setTechnology ( intent.extras!!.getString(BatteryManager.EXTRA_TECHNOLOGY))
            battery!!.setTemperature( intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0))
            battery!!.setVoltage ( intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0))
            battery!!.setCharging ( battery!!.getStatus() == BatteryManager.BATTERY_STATUS_CHARGING ||
                    battery!!.getStatus() == BatteryManager.BATTERY_STATUS_FULL)
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    fun makeBattery(intent: Intent): Battery {
        val battery = Battery()
        battery.setHealth (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0))
        battery.setIconSmall ( intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0))
        battery.setLevel (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0))
        battery.setPlugged (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0))
        battery.setPresent (intent.extras!!.getBoolean(BatteryManager.EXTRA_PRESENT))
        battery.setScale(intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0))
        battery.setStatus ( intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0))
        battery.setTechnology ( intent.extras!!.getString(BatteryManager.EXTRA_TECHNOLOGY))
        battery.setTemperature( intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0))
        battery.setVoltage ( intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0))
        battery.setCharging ( battery.getStatus() == BatteryManager.BATTERY_STATUS_CHARGING ||
                battery.getStatus() == BatteryManager.BATTERY_STATUS_FULL)
        return battery
    }
}