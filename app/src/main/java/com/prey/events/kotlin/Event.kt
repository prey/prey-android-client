/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.kotlin

class Event {
    var name: String? = null
    var info: String? = null
    var isAlwaysSend: Boolean = false

    constructor()

    constructor(name: String?) {
        this.name = name
        this.info = ""
        this.isAlwaysSend = false
    }

    constructor(name: String?, info: String?) {
        this.name = name
        this.info = info
    }

    companion object {
        const val SIM_CHANGED: String = "sim_changed"
        const val WIFI_CHANGED: String = "ssid_changed"
        const val TURNED_ON: String = "device_turned_on"
        const val TURNED_OFF: String = "device_turned_off"
        const val BATTERY_LOW: String = "low_battery"
        const val APPLICATION_OPENED: String = "prey_opened"
        const val POWER_CONNECTED: String = "started_charging"
        const val POWER_DISCONNECTED: String = "stopped_charging"
        const val PIN_CHANGED: String = "pin_changed"
        const val MOBILE_CONNECTED: String = "mobile_network"
        const val DEVICE_STATUS: String = "device_status"
        const val DEVICE_RENAMED: String = "device_renamed"
        const val DEVICE_ADDED: String = "device_added"
        const val NATIVE_LOCK: String = "native_lock"
        const val ANDROID_LOCK_PIN: String = "android_lock_pin"
    }
}