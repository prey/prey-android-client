/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.factories

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPhone
import com.prey.actions.aware.AwareInitialLocationProvider
import com.prey.actions.location.daily.DailyLocationUtil.enqueueDailyCheck
import com.prey.actions.triggers.BatteryTriggerReceiver
import com.prey.actions.triggers.SimTriggerReceiver
import com.prey.events.Event
import com.prey.managers.PreyConnectivityManager
import com.prey.net.UtilConnection
import org.json.JSONObject

/**
 * Factory object responsible for transforming Android system [Intent] broadcasts into
 * Prey-specific [Event] instances.
 *
 * This object acts as a central dispatcher that intercepts various system actions—such as
 * battery status, SIM card changes, Wi-Fi connectivity, and location provider updates—to
 * generate the appropriate domain events or trigger background synchronization tasks.
 */
object EventFactory {

    const val SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
    const val LOCATION_MODE_CHANGED = "android.location.MODE_CHANGED"
    const val BATTERY_LOW = Intent.ACTION_BATTERY_LOW
    const val ACTION_POWER_CONNECTED = Intent.ACTION_POWER_CONNECTED
    const val ACTION_POWER_DISCONNECTED = Intent.ACTION_POWER_DISCONNECTED
    const val LOCATION_PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED"
    const val WIFI_STATE_CHANGED = WifiManager.WIFI_STATE_CHANGED_ACTION
    const val USER_PRESENT = Intent.ACTION_USER_PRESENT

    /**
     * Parses an incoming [Intent] to determine if it corresponds to a specific system event
     * monitored by Prey.
     *
     * This function acts as a factory, mapping Android system actions (like boot, shutdown,
     * battery changes, or connectivity shifts) to Prey [Event] objects. Some actions may
     * trigger background processes without returning a visible event.
     *
     * @param context The application context used to handle system services and preferences.
     * @param intent The broadcasted intent containing the action and extra data to be processed.
     * @return An [Event] object if the action matches a tracked event, or `null` if the action
     *         is ignored or handled internally without a resulting event.
     */
    fun getEvent(context: Context, intent: Intent): Event? {
        val action = intent.action ?: return null
        PreyLogger.d("getEvent[$action]")
        return when (action) {
            Intent.ACTION_BOOT_COMPLETED -> Event(Event.TURNED_ON)
            Intent.ACTION_SHUTDOWN -> Event(Event.TURNED_OFF)
            SIM_STATE_CHANGED -> handleSimState(context, intent)
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> handleBatteryEvent(context, intent, action)

            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                handleAirplaneMode(context, intent); null
            }

            LOCATION_PROVIDERS_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            LOCATION_MODE_CHANGED -> {
                handleLocationChange(context); null
            }

            WifiManager.WIFI_STATE_CHANGED_ACTION -> handleWifiChange(context, intent)
            else -> null
        }
    }

    /**
     * Processes battery-related broadcast intents, delegates the event to the [BatteryTriggerReceiver],
     * and maps the intent action to its corresponding [Event] type.
     *
     * @param context The application context.
     * @param intent The broadcast intent received.
     * @param action The action string of the intent (e.g., ACTION_BATTERY_LOW).
     * @return An [Event] representing the specific battery status change.
     */
    private fun handleBatteryEvent(context: Context, intent: Intent, action: String): Event {
        BatteryTriggerReceiver().onReceive(context, intent)
        return when (action) {
            Intent.ACTION_BATTERY_LOW -> Event(Event.BATTERY_LOW)
            Intent.ACTION_POWER_CONNECTED -> Event(Event.POWER_CONNECTED)
            else -> Event(Event.POWER_DISCONNECTED)
        }
    }

    /**
     * Handles changes in the SIM card state.
     *
     */
    private fun handleSimState(context: Context, intent: Intent): Event? {
        val state = intent.getStringExtra(SimTriggerReceiver.EXTRA_SIM_STATE)
        if (state == "ABSENT") {
            SimTriggerReceiver().onReceive(context, intent)
            if (!UtilConnection.isInternetAvailable(context)) return null
            val simSerial = PreyConfig.getPreyConfig(context).simSerialNumber
            val info = JSONObject().apply {
                if (!simSerial.isNullOrBlank()) put("sim_serial_number", simSerial)
            }
            return Event(Event.SIM_CHANGED, info.toString())
        }
        return null
    }

    /**
     * Handles changes in location settings, providers, or system locale.
     *
     * This function verifies if the required [Manifest.permission.ACCESS_FINE_LOCATION]
     * permission is granted. If authorized, it initializes the [AwareInitialLocationProvider]
     * to ensure location services are updated and tracking is synchronized.
     *
     * @param context The application context used for permission checks and service initialization.
     */
    private fun handleLocationChange(context: Context) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            AwareInitialLocationProvider(context).init()
        }
    }

    /**
     * Handles changes in the device's Wi-Fi state based on the provided broadcast intent.
     *
     * This function extracts the current Wi-Fi state and prepares an [Event] with a JSON
     * payload indicating whether the device is now using Wi-Fi or mobile data. If Wi-Fi
     * is enabled, it also triggers a daily check synchronization.
     *
     * @param context The application context.
     * @param intent The [WifiManager.WIFI_STATE_CHANGED_ACTION] intent containing the new state.
     * @return An [Event] object representing the Wi-Fi status change.
     */
    private fun handleWifiChange(context: Context, intent: Intent): Event {
        val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
        val info = JSONObject().apply {
            when (wifiState) {
                WifiManager.WIFI_STATE_ENABLED -> {
                    put("connected", "wifi")
                    enqueueDailyCheck(context)
                }

                WifiManager.WIFI_STATE_DISABLED -> put("connected", "mobile")
            }
        }
        return Event(Event.WIFI_CHANGED, info.toString())
    }

    /**
     * Responds to airplane mode transitions by checking for network reconnection.
     *
     * If airplane mode has been disabled and either Wi-Fi or mobile data services
     */
    private fun handleAirplaneMode(context: Context, intent: Intent) {
        if (PreyPhone.isAirplaneModeOn(context)) return
        val connManager = PreyConnectivityManager.getInstance(context)
        val isWifiReconnected = !connManager.isWifiConnected &&
                intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1) == WifiManager.WIFI_STATE_ENABLED
        val isMobileReconnected = !connManager.isMobileConnected &&
                intent.getStringExtra(ConnectivityManager.EXTRA_REASON) == "connected"
        if (isWifiReconnected || isMobileReconnected) {
            enqueueDailyCheck(context)
        }
    }

}