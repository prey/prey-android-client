/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.factories.kotlin

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import com.prey.actions.aware.kotlin.AwareController
import com.prey.actions.location.kotlin.LocationUtil
import com.prey.actions.triggers.kotlin.BatteryTriggerReceiver
import com.prey.actions.triggers.kotlin.SimTriggerReceiver
import com.prey.beta.actions.kotlin.PreyBetaController
import com.prey.events.kotlin.Event
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPermission
import com.prey.managers.kotlin.PreyConnectivityManager
import com.prey.net.kotlin.UtilConnection
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object EventFactory {
    const val BOOT_COMPLETED: String = "android.intent.action.BOOT_COMPLETED"
    const val CONNECTIVITY_CHANGE: String = "android.net.conn.CONNECTIVITY_CHANGE"
    const val WIFI_STATE_CHANGED: String = "android.net.wifi.WIFI_STATE_CHANGED"
    const val ACTION_SHUTDOWN: String = "android.intent.action.ACTION_SHUTDOWN"
    const val AIRPLANE_MODE: String = "android.intent.action.AIRPLANE_MODE"
    const val BATTERY_LOW: String = "android.intent.action.BATTERY_LOW"
    const val SIM_STATE_CHANGED: String = "android.intent.action.SIM_STATE_CHANGED"
    const val USER_PRESENT: String = "android.intent.action.USER_PRESENT"
    const val ACTION_POWER_CONNECTED: String = "android.intent.action.ACTION_POWER_CONNECTED"
    const val ACTION_POWER_DISCONNECTED: String = "android.intent.action.ACTION_POWER_DISCONNECTED"
    const val LOCATION_MODE_CHANGED: String = "android.location.MODE_CHANGED"
    const val LOCATION_PROVIDERS_CHANGED: String = "android.location.PROVIDERS_CHANGED"
    const val NOTIFICATION_ID: Int = 888

    fun getEvent(ctx: Context, intent: Intent): Event? {
        val message = "getEvent[" + intent.action + "]"
        PreyLogger.d(message)
        if (BOOT_COMPLETED == intent.action) {
            notification(ctx)
            return Event(Event.TURNED_ON)
        }
        if (SIM_STATE_CHANGED == intent.action) {
            val state = intent.extras!!.getString(SimTriggerReceiver.EXTRA_SIM_STATE)
            if ("ABSENT" == state) {
                val info = JSONObject()
                try {
                    val simSerial = PreyConfig.getInstance(ctx).getSimSerialNumber()
                    if (simSerial != null && "" != simSerial) {
                        info.put("sim_serial_number", simSerial)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
                SimTriggerReceiver().onReceive(ctx, intent)
                return if (UtilConnection.getInstance().isInternetAvailable()) {
                    Event(Event.SIM_CHANGED, info.toString())
                } else {
                    null
                }
            }
        }
        if (LOCATION_PROVIDERS_CHANGED == intent.action || LOCATION_MODE_CHANGED == intent.action
        ) {
            object : Thread() {
                override fun run() {
                    sendLocationAware(ctx)
                }
            }.start()
        }
        if (ACTION_SHUTDOWN == intent.action) {
            return Event(Event.TURNED_OFF)
        }
        if (BATTERY_LOW == intent.action) {
            BatteryTriggerReceiver().onReceive(ctx, intent)
            return Event(Event.BATTERY_LOW)
        }
        if (ACTION_POWER_CONNECTED == intent.action) {
            BatteryTriggerReceiver().onReceive(ctx, intent)
            return Event(Event.POWER_CONNECTED)
        }
        if (ACTION_POWER_DISCONNECTED == intent.action) {
            BatteryTriggerReceiver().onReceive(ctx, intent)
            return Event(Event.POWER_DISCONNECTED)
        }
        if (CONNECTIVITY_CHANGE == intent.action) {
            return null
        }
        if (WIFI_STATE_CHANGED == intent.action) {
            val info = JSONObject()
            val wifiState =
                intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
            PreyLogger.d("getEvent ___wifiState:$wifiState")
            try {
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    PreyLogger.d("getEvent wifiState connected")
                    info.put("connected", "wifi")
                    PreyBetaController.getInstance().startPrey(ctx)
                }
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    PreyLogger.d("getEvent mobile connected")
                    info.put("connected", "mobile")
                }
            } catch (e: Exception) {
                PreyLogger.e("Error getEvent:" + e.message, e)
            }
            return Event(Event.WIFI_CHANGED, info.toString())
        }
        if (AIRPLANE_MODE == intent.action) {
            if (!isAirplaneModeOn(ctx)) {
                val verifyNotification = verifyNotification(ctx)
                if (!verifyNotification) {
                    notification(ctx)
                }
                var connected = false
                if (!PreyConnectivityManager.getInstance().isWifiConnected(ctx)) {
                    val extras = intent.extras
                    if (extras != null) {
                        if ("connected" == extras.getString(ConnectivityManager.EXTRA_REASON)) {
                            connected = true
                        }
                    }
                }
                if (!PreyConnectivityManager.getInstance().isMobileConnected(ctx)) {
                    val wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN
                    )
                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        connected = true
                    }
                }
                if (connected) {
                    PreyBetaController.getInstance().startPrey(ctx)
                }
            }
        }
        if (USER_PRESENT == intent.action) {
            PreyLogger.d("EventFactory USER_PRESENT")
            val minuteScheduled = PreyConfig.getInstance(ctx).getMinuteScheduled()
            if (minuteScheduled > 0) {
                PreyBetaController.getInstance().startPrey(ctx, null)
            }
            return null
        }
        return null
    }

    fun sendLocationAware(ctx: Context) {
        try {
            val isTimeLocationAware = PreyConfig.getInstance(ctx).isTimeLocationAware()
            PreyLogger.d("sendLocation isTimeLocationAware:$isTimeLocationAware")
            if (!isTimeLocationAware) {
                val locationNow = LocationUtil.getLocation(ctx, null, false)
                AwareController.getInstance().sendAware(ctx, locationNow)

                PreyConfig.getInstance(ctx).setTimeLocationAware()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error sendLocation:" + e.message, e)
        }
    }

    fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.AIRPLANE_MODE_ON,
            0
        ) != 0
    }

    private val sdf = SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault())

    fun isValidLowBattery(ctx: Context): Boolean {
        try {
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.MINUTE, -1)
            val leastThreeHours = cal.timeInMillis
            val lowBatteryDate = PreyConfig.getInstance(ctx).getLowBatteryDate()
            PreyLogger.d("lowBatteryDate :" + lowBatteryDate + " " + sdf.format(Date(lowBatteryDate)))
            PreyLogger.d(
                "leastMinutes   :" + leastThreeHours + " " + sdf.format(
                    Date(
                        leastThreeHours
                    )
                )
            )
            if (lowBatteryDate == 0L || leastThreeHours > lowBatteryDate) {
                val now = Date().time
                PreyConfig.getInstance(ctx).setLowBatteryDate (now)
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Method that returns if it has all the permissions
     *
     * @param ctx context
     * @return if you have all permissions
     */
    fun verifyNotification(ctx: Context?): Boolean {
        val canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(ctx)
        val canAccessFineLocation = PreyPermission.canAccessFineLocation(ctx)
        val canAccessStorage = PreyPermission.canAccessStorage(ctx)
        return (canAccessCoarseLocation || canAccessFineLocation) && canAccessStorage
    }

    /**
     * Method that opens the notification missing permissions
     *
     * @param ctx context
     */
    fun notification(ctx: Context?) {
    }
}