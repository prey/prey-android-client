/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.battery.kotlin

class Battery {
    private var health: Int = 0
    private var iconSmall: Int = 0
    private var level: Int = 0
    private var plugged: Int = 0
    private var isPresent: Boolean = false
    private var scale: Int = 0
    private var status: Int = 0
    private var technology: String? = null
    private var temperature: Int = 0
    private var voltage: Int = 0
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