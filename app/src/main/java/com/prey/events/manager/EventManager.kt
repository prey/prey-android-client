/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.SystemClock
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPhone
import com.prey.PreyPhoneKt
import com.prey.events.Event
import com.prey.json.actions.LocationLowBattery.getCoroutine
import com.prey.json.parser.JsonCommandDispatcher.getActionsJson
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Singleton object responsible for managing, processing, and dispatching device events
 * to the Prey web services.
 *
 * This manager handles the lifecycle of an event, including:
 * - Determining the current network state (WiFi vs Mobile).
 * - Implementing retry logic to ensure connection details are captured correctly.
 * - Gathering system telemetry such as battery status and uptime.
 * - Avoiding duplicate event submissions to optimize data usage.
 * - Dispatching event payloads asynchronously using [CoroutineScope] with [Dispatchers.IO].
 *
 * @property MOBILE Constant string identifying a mobile data connection.
 * @property WIFI Constant string identifying a wireless network connection.
 */
object EventManager {

    const val MOBILE: String = "mobile"
    const val WIFI: String = "wifi"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    /**
     * Starts the asynchronous processing of a specific event using the internal [scope].
     *
     * This method acts as a non-suspending bridge to the [process] function, allowing
     * events to be handled in the background without blocking the calling thread.
     *
     * @param context The Android context used to access system services and configuration.
     * @param event The [Event] object representing the detected change or status to be processed.
     */
    fun processCoroutine(context: Context, event: Event?) {
        scope.launch { process(context, event) }
    }

    /**
     * Processes a specific event by validating the device's registration and connectivity status,
     * then gathers system information (WiFi, Mobile data, Battery) to be sent to Prey's servers.
     *
     * The process includes:
     * 1. Verifying if the device is registered.
     * 2. Determining the required search type (WiFi or Mobile) based on the event.
     * 3. Executing a retry logic to wait for a valid network connection.
     * 4. Building the JSON payload with connectivity and battery metadata.
     * 5. Dispatching the event via [sendEvent].
     *
     * @param context The application context.
     * @param event The [Event] to be processed. If null, the operation is aborted.
     */
    suspend fun process(context: Context, event: Event?) {
        if (event == null) return
        val config = PreyConfig.getPreyConfig(context)
        if (!config.isThisDeviceAlreadyRegisteredWithPrey()) return
        //Determine what we are looking for based on the event or network state
        val (isWifiSearch, isMobileSearch) = determineSearchType(context, event)
        var connectionType: String? = null
        var isValid = false
        //Retry logic for WiFi
        if (isWifiSearch) {
            retrySuspending(10) {
                val ssid = PreyPhoneKt.getWifi(context)?.ssid?.replace("\"", "")
                if (isValidSsid(ssid)) {
                    isValid = true
                    event.info = ssid
                    connectionType = PreyPhoneKt.CONNECTION_WIFI
                    PreyLogger.d("Connected to WIFI:$ssid")
                    true
                } else false
            }
        }
        //Retry logic for Mobile (if not validated with WiFi)
        if (isMobileSearch && !isValid) {
            retrySuspending(10) {
                val networkType = PreyPhoneKt.getNetworkTypeLegacy(context)
                if (networkType != null) {
                    isValid = true
                    event.info = networkType
                    connectionType = PreyPhoneKt.CONNECTION_MOBILE
                    PreyLogger.d("Connected to Mobile Data:$networkType")
                    true
                } else false
            }
        }
        if (event.name == Event.DEVICE_STATUS) {
            isValid = true
        }
        //Ending
        PreyLogger.d("process isValid:$isValid connectionType:$connectionType")
        if (isValid) {
            val batteryInfo = getBatteryData(context)
            val eventPayload = buildEventJson(context, event, connectionType, batteryInfo)
            sendEvent(context, event, eventPayload)
        }
    }

    /**
     * Executes a suspending block multiple times until it returns true or the maximum number of attempts is
     */
    private suspend fun retrySuspending(times: Int, delayMs: Long = 1000, block: suspend () -> Boolean) {
        repeat(times) { i ->
            runCatching { if (block()) return@retrySuspending }.onFailure {
                PreyLogger.e("Retry failed ${i + 1}: ${it.message}", it)
            }
            if (i < times - 1) delay(delayMs)
        }
    }

    /**
     * Determines whether to search for WiFi, Mobile Data, or both based on the event type.
     *
     * @param context The application context to access system services.
     * @param event The event to evaluate.
     * @return A [Pair] where the first value is true if a WiFi search is required,
     *         and the second value is true if a Mobile Data search is required.
     */
    private fun determineSearchType(context: Context, event: Event): Pair<Boolean, Boolean> {
        if (event.name == Event.WIFI_CHANGED) {
            val info = event.info ?: ""
            return Pair(info.contains("wifi"), info.contains("mobile"))
        }
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetworkInfo
        return when (activeNetwork?.type) {
            ConnectivityManager.TYPE_WIFI -> true to false
            ConnectivityManager.TYPE_MOBILE -> false to true
            else -> false to false
        }
    }

    /**
     * Validates if the provided SSID is a legitimate network identifier.
     *
     * It filters out null, blank, or Android-specific placeholder values
     * like "<unknown ssid>" or "0x" which are often returned when
     * location services are disabled or the scan is incomplete.
     *
     * @param ssid The SSID string to validate.
     * @return true if the SSID is valid, false otherwise.
     */
    private fun isValidSsid(ssid: String?): Boolean {
        return !ssid.isNullOrBlank() && ssid != "<unknown ssid>" && ssid != "0x"
    }

    /**
     * Builds a [JSONObject] containing the hardware and network state metadata for a specific event.
     *
     * This includes system uptime, network connection details (SSID for WiFi or network type for mobile),
     * and current battery status (charging state and percentage).
     *
     * @param context The application context used to retrieve network information.
     * @param event The [Event] being processed.
     * @param connectType The type of connection detected (e.g., [PreyPhoneKt.CONNECTION_MOBILE] or [PreyPhoneKt.CONNECTION_WIFI]).
     * @param battery A [BatteryInfo] object containing the current battery level and charging state.
     * @return A [JSONObject] populated with the device's current state metadata.
     */
    private fun buildEventJson(
        context: Context,
        event: Event,
        connectType: String?,
        battery: BatteryInfo
    ) = JSONObject().apply {
        put("uptime", SystemClock.uptimeMillis().toString())
        put("online", true)
        if (connectType == PreyPhoneKt.CONNECTION_MOBILE) {
            put("mobile_internet", PreyPhoneKt.getNetworkTypeLegacy(context))
        } else {
            PreyPhoneKt.getWifi(context)?.let { wifi ->
                put("active_access_point", buildWifiJson(context, event, wifi.ssid))
            }
        }
        put("battery_status", JSONObject().apply {
            put("state", if (battery.isCharging) "charging" else "discharging")
            put("percentage_remaining", battery.percentage)
        })
    }

    /**
     * Builds a [JSONObject] containing detailed information about a specific Wi-Fi network.
     *
     * This method cleans the SSID by removing extra quotes and, if the event is not a simple
     * device status check, attempts to fetch additional network metadata such as signal strength,
     * frequency channel, and security capabilities from the system's scanned Wi-Fi list.
     *
     * @param context The application context used to access Wi-Fi services.
     * @param event The current [Event] being processed to determine the level of detail required.
     * @param rawSsid The raw SSID string, which may contain surrounding quotes.
     * @return A [JSONObject] populated with Wi-Fi details (ssid, signal_strength, channel, security).
     */
    private fun buildWifiJson(context: Context, event: Event, rawSsid: String?) = JSONObject().apply {
        val cleanSsid = rawSsid?.replace("\"", "") ?: ""
        put("ssid", cleanSsid)
        PreyLogger.d("buildWifiJson name:[${cleanSsid}]")
        if (event.name != Event.DEVICE_STATUS) {
            PreyPhoneKt.getListWifi(context).find { it.SSID == cleanSsid }?.let { item ->
                put("signal_strength", item.level.toString())
                put("channel", PreyPhone.channelsFrequency.indexOf(item.frequency))
                put("security", item.capabilities)
            }
        }
    }

    /**
     * Prepares and sends an event to Prey's web services asynchronously.
     */
    fun sendEvent(context: Context, event: Event, json: JSONObject) {
        val config = PreyConfig.getPreyConfig(context)
        val lastEventTag = config.lastEvent
        val currentEventTag = "${event.name}_${event.info}"
        PreyLogger.d("Processing event: ${event.name} | Info: ${event.info}")
        //Low Battery Logic (Encapsulated to avoid contaminating the main flow)
        handleLowBatteryLegacy(context, event, config, json)
        //Preparing the event information
        when (event.name) {
            Event.DEVICE_STATUS -> event.info = json.toString()
            Event.WIFI_CHANGED -> { /* Keep information */
            }

            else -> event.info = ""
        }
        val isWifiChanged = event.name == Event.WIFI_CHANGED
        val isNewEvent = currentEventTag != lastEventTag
        if (!isWifiChanged || isNewEvent) {
            scope.launch {
                runCatching {
                    PreyWebServicesKt.sendPreyHttpEvent(context, event, json)
                }.onSuccess { response ->
                    if (!response.isNullOrBlank()) {
                        config.lastEvent = currentEventTag
                        PreyLogger.d("Event sent successfully: ${event.name}")
                        getActionsJson(context, response)
                    }
                }.onFailure { e ->
                    PreyLogger.e("Error sending event ${event.name}: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Handles the legacy logic for low battery events.
     *
     * If the current event is [Event.BATTERY_LOW] and the low battery location feature
     * is enabled in the configuration, it triggers a location update via a coroutine
     * and updates the provided JSON payload.
     *
     * @param context The application context.
     * @param event The event being processed.
     * @param config The current Prey configuration.
     * @param json The JSON object where the "locationLowBattery" flag will be added if triggered.
     */
    private fun handleLowBatteryLegacy(context: Context, event: Event, config: PreyConfig, json: JSONObject) {
        if (event.name == Event.BATTERY_LOW && config.isLocationLowBattery) {
            if (LocationLowBatteryRunner.isValid(context)) {
                getCoroutine(context, JSONObject())
                json.put("locationLowBattery", true)
            }
        }
    }

    /**
     * Retrieves the current battery status and percentage from the system.
     *
     * This method registers a receiver for the sticky [Intent.ACTION_BATTERY_CHANGED]
     * broadcast to extract battery level, scale, and charging status.
     *
     * @param context The application or activity context.
     * @return A [BatteryInfo] object containing the current battery percentage and charging state.
     */
    fun getBatteryData(context: Context): BatteryInfo {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter) ?: return BatteryInfo(0, false)
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val pct = if (level != -1 && scale != -1) (level * 100 / scale) else 0
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        return BatteryInfo(pct, isCharging)
    }
}

/**
 * Data class representing the battery state of the device.
 *
 * @property percentage The current battery level from 0 to 100.
 * @property isCharging True if the device is currently plugged into a power source or fully charged.
 */
data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean
)