/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.battery

/**
 * Represents a battery with its properties and behaviors.
 */
class Battery {
    /**
     * The health of the battery, ranging from 0 (unknown) to 5 (good).
     */
    private var health: Int = 0
    /**
     * The small icon representing the battery level.
     */
    private var iconSmall: Int = 0
    /**
     * The current battery level, ranging from 0 (empty) to 100 (full).
     */
    private var level: Int = 0
    /**
     * The type of power source currently connected to the device.
     */
    private var plugged: Int = 0
    /**
     * Whether the battery is present in the device.
     */
    private var isPresent: Boolean = false
    /**
     * The maximum battery capacity.
     */
    private var scale: Int = 0
    /**
     * The current battery status (e.g., charging, discharging, full).
     */
    private var status: Int = 0
    /**
     * The technology used by the battery (e.g., Li-ion, NiMH).
     */
    private var technology: String? = null
    /**
     * The current battery temperature in degrees Celsius.
     */
    private var temperature: Int = 0
    /**
     * The current battery voltage in millivolts.
     */
    private var voltage: Int = 0
    /**
     * Whether the battery is currently charging.
     */
    private var isCharging: Boolean = false

    fun isCharging(): Boolean {
        return isCharging
    }

    fun setCharging(isCharging: Boolean) {
        this.isCharging = isCharging
    }

    fun getHealth(): Int {
        return health
    }

    fun setHealth(health: Int) {
        this.health = health
    }

    fun getIconSmall(): Int {
        return iconSmall
    }

    fun setIconSmall(iconSmall: Int) {
        this.iconSmall = iconSmall
    }

    fun getLevel(): Int {
        return level
    }

    fun setLevel(level: Int) {
        this.level = level
    }

    fun getPlugged(): Int {
        return plugged
    }

    fun setPlugged(plugged: Int) {
        this.plugged = plugged
    }

    fun isPresent(): Boolean {
        return isPresent
    }

    fun setPresent(isPresent: Boolean) {
        this.isPresent = isPresent
    }

    fun getScale(): Int {
        return scale
    }

    fun setScale(scale: Int) {
        this.scale = scale
    }

    fun getStatus(): Int {
        return status
    }

    fun setStatus(status: Int) {
        this.status = status
    }

    fun getTechnology(): String {
        return technology!!
    }

    fun setTechnology(technology: String?) {
        this.technology = technology
    }

    fun getTemperature(): Int {
        return temperature
    }

    fun setTemperature(temperature: Int) {
        this.temperature = temperature
    }

    fun getVoltage(): Int {
        return voltage
    }

    fun setVoltage(voltage: Int) {
        this.voltage = voltage
    }
}