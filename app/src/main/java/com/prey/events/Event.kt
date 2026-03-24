/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events

/**
 * Represents a system or application event within the Prey framework.
 *
 * This data class is used to encapsulate event information, typically triggered by
 * device state changes, security actions, or hardware status updates.
 *
 * @property name The identifier or type of the event, usually one of the constants defined in [Companion].
 * @property info Optional additional information or metadata associated with the event.
 */
data class Event(
    var name: String,
    var info: String? = "",
) {
    constructor (name: String) : this(name, "")

    companion object {
        const val SIM_CHANGED = "sim_changed"
        const val WIFI_CHANGED = "ssid_changed"
        const val TURNED_ON = "device_turned_on"
        const val TURNED_OFF = "device_turned_off"
        const val BATTERY_LOW = "low_battery"
        const val APPLICATION_OPENED = "prey_opened"
        const val POWER_CONNECTED = "started_charging"
        const val POWER_DISCONNECTED = "stopped_charging"
        const val PIN_CHANGED = "pin_changed"
        const val MOBILE_CONNECTED = "mobile_network"
        const val DEVICE_STATUS = "device_status"
        const val DEVICE_RENAMED = "device_renamed"
        const val DEVICE_ADDED = "device_added"
        const val NATIVE_LOCK = "native_lock"
        const val ANDROID_LOCK_PIN = "android_lock_pin"
    }

}