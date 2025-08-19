/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.factories

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings

import com.prey.actions.aware.AwareController
import com.prey.actions.location.LocationUtil
import com.prey.actions.triggers.BatteryTriggerReceiver
import com.prey.actions.triggers.SimTriggerReceiver
import com.prey.beta.actions.PreyBetaController
import com.prey.events.Event
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.net.UtilConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * EventFactory is responsible for creating events based on system intents.
 */
object EventFactory {
    // Constants for system intents
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

    /**
     * Creates an event based on the given system intent.
     *
     * @param context The application context.
     * @param intent The system intent.
     * @return The created event, or null if no event is applicable.
     */
    fun getEvent(context: Context, intent: Intent): Event? {
        // Determine the event type based on the intent action
        when (intent.action) {
            // Device boot completed
            BOOT_COMPLETED -> return Event(Event.TURNED_ON)
            // SIM state changed
            SIM_STATE_CHANGED -> {
                val state = intent.extras?.getString(SimTriggerReceiver.EXTRA_SIM_STATE)
                if (state == "ABSENT") {
                    val info = JSONObject()
                    try {
                        val simSerial = PreyConfig.getInstance(context).getSimSerialNumber()
                        if (simSerial != null && simSerial.isNotEmpty()) {
                            info.put("sim_serial_number", simSerial)
                        }
                    } catch (e: Exception) {
                        // Handle exception
                    }
                    SimTriggerReceiver().onReceive(context, intent)
                    return if (UtilConnection.getInstance().isInternetAvailable()) {
                        Event(Event.SIM_CHANGED, info.toString())
                    } else {
                        null
                    }
                }
            }
            // Location mode changed or providers changed
            LOCATION_MODE_CHANGED, LOCATION_PROVIDERS_CHANGED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    sendLocationAware(context)
                }
            }
            // Device shutdown
            ACTION_SHUTDOWN -> return Event(Event.TURNED_OFF)
            // Battery low
            BATTERY_LOW -> {
                BatteryTriggerReceiver().onReceive(context, intent)
                return Event(Event.BATTERY_LOW)
            }
            // Power connected or disconnected
            ACTION_POWER_CONNECTED, ACTION_POWER_DISCONNECTED -> {
                BatteryTriggerReceiver().onReceive(context, intent)
                return if (intent.action == ACTION_POWER_CONNECTED) {
                    Event(Event.POWER_CONNECTED)
                } else {
                    Event(Event.POWER_DISCONNECTED)
                }
            }
            // Connectivity change
            CONNECTIVITY_CHANGE -> return null
            // WiFi state changed
            WIFI_STATE_CHANGED -> {
                val info = JSONObject()
                val wifiState =
                    intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                try {
                    when (wifiState) {
                        WifiManager.WIFI_STATE_ENABLED -> {
                            info.put("connected", "wifi")
                            PreyBetaController.getInstance().startPrey(context)
                        }

                        WifiManager.WIFI_STATE_DISABLED -> {
                            info.put("connected", "mobile")
                        }
                    }
                } catch (e: Exception) {
                    // Handle exception
                }
                return Event(Event.WIFI_CHANGED, info.toString())
            }
            // Airplane mode
            AIRPLANE_MODE -> {
                if (!isAirplaneModeOn(context)) {
                    // Check if connected to wifi or mobile
                    PreyBetaController.getInstance().startPrey(context)
                }
            }
            // User present
            USER_PRESENT -> {
                val minuteScheduled = PreyConfig.getInstance(context).getMinuteScheduled()
                if (minuteScheduled > 0) {
                    PreyBetaController.getInstance().startPrey(context, null)
                }
                return null
            }
        }
        return null
    }

    /**
     * Sends a location-aware notification if the time threshold has been met.
     *
     * @param context The application context.
     */
    fun sendLocationAware(context: Context) {
        try {
            val isTimeLocationAware = PreyConfig.getInstance(context).isTimeLocationAware()
            PreyLogger.d("sendLocation isTimeLocationAware:$isTimeLocationAware")
            if (!isTimeLocationAware) {
                val locationNow = LocationUtil.getLocation(context, null, false)
                if (locationNow != null) {
                    AwareController.getInstance().sendAware(context, locationNow)
                }
                PreyConfig.getInstance(context).setTimeLocationAware()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error sendLocation:${e.message}", e)
        }
    }

    /**
     * Checks if airplane mode is currently enabled on the device.
     *
     * @param context The application context.
     * @return True if airplane mode is enabled, false otherwise.
     */
    public fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.AIRPLANE_MODE_ON,
            0
        ) != 0
    }

    private val sdf = SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault())

    /**
     * Checks if the device's battery level is low and if a notification should be sent.
     *
     * @param context The application context.
     * @return True if the battery level is low and a notification should be sent, false otherwise.
     */
    fun isValidLowBattery(context: Context): Boolean {
        try {
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.MINUTE, -1)
            val leastThreeHours = cal.timeInMillis
            val lowBatteryDate = PreyConfig.getInstance(context).getLowBatteryDate()
            PreyLogger.d("lowBatteryDate :${lowBatteryDate} ${sdf.format(Date(lowBatteryDate))}")
            PreyLogger.d("leastMinutes   :${leastThreeHours} ${sdf.format(Date(leastThreeHours))}")
            if (lowBatteryDate == 0L || leastThreeHours > lowBatteryDate) {
                PreyConfig.getInstance(context).setLowBatteryDate(Date().time)
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
     * @param context context
     * @return if you have all permissions
     */
    fun verifyNotification(context: Context): Boolean {
        val canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(context)
        val canAccessFineLocation = PreyPermission.canAccessFineLocation(context)
        val canAccessStorage = PreyPermission.canAccessStorage(context)
        return (canAccessCoarseLocation || canAccessFineLocation) && canAccessStorage
    }

}