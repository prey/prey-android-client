/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

/**
 * Represents the hardware information of a device.
 */
class PreyHardware {
    private var uuid: String? = null
    private var biosVendor: String? = null
    private var biosVersion: String? = null
    private var mbVendor: String? = null
    private var mbSerial: String? = null
    private var mbModel: String? = null
    private var mbVersion: String? = null
    private var cpuModel: String? = null
    private var cpuSpeed: String? = null
    private var cpuCores: String? = null
    private var ramSize: String? = null
    private var ramModules: String? = null
    private var serialNumber: String? = null
    private var totalMemory: Long = 0
    private var freeMemory: Long = 0
    private var busyMemory: Long = 0
    private var androidDeviceId: String? = null

    fun getTotalMemory(): Long {
        return totalMemory
    }

    fun setTotalMemory(totalMemory: Long) {
        this.totalMemory = totalMemory
    }

    fun getFreeMemory(): Long {
        return freeMemory
    }

    fun setFreeMemory(freeMemory: Long) {
        this.freeMemory = freeMemory
    }

    fun getBusyMemory(): Long {
        return busyMemory
    }

    fun setBusyMemory(busyMemory: Long) {
        this.busyMemory = busyMemory
    }

    fun getRamSize(): String {
        return ramSize!!
    }

    fun setRamSize(ramSize: String?) {
        this.ramSize = ramSize
    }

    fun getRamModules(): String {
        return ramModules!!
    }

    fun setRamModules(ramModules: String?) {
        this.ramModules = ramModules
    }

    fun getSerialNumber(): String {
        return serialNumber!!
    }

    fun setSerialNumber(serialNumber: String?) {
        this.serialNumber = serialNumber
    }

    fun getUuid(): String {
        return uuid!!
    }

    fun setUuid(uuid: String?) {
        this.uuid = uuid
    }

    fun getBiosVendor(): String {
        return biosVendor!!
    }

    fun setBiosVendor(biosVendor: String?) {
        this.biosVendor = biosVendor
    }

    fun getBiosVersion(): String {
        return biosVersion!!
    }

    fun setBiosVersion(biosVersion: String?) {
        this.biosVersion = biosVersion
    }

    fun getMbVendor(): String {
        return mbVendor!!
    }

    fun setMbVendor(mbVendor: String?) {
        this.mbVendor = mbVendor
    }

    fun getMbSerial(): String {
        return mbSerial!!
    }

    fun setMbSerial(mbSerial: String?) {
        this.mbSerial = mbSerial
    }

    fun getMbModel(): String {
        return mbModel!!
    }

    fun setMbModel(mbModel: String?) {
        this.mbModel = mbModel
    }

    fun getMbVersion(): String {
        return mbVersion!!
    }

    fun setMbVersion(mbVersion: String?) {
        this.mbVersion = mbVersion
    }

    fun getCpuModel(): String {
        return cpuModel!!
    }

    fun setCpuModel(cpuModel: String?) {
        this.cpuModel = cpuModel
    }

    fun getCpuSpeed(): String {
        return cpuSpeed!!
    }

    fun setCpuSpeed(cpuSpeed: String?) {
        this.cpuSpeed = cpuSpeed
    }

    fun getCpuCores(): String {
        return cpuCores!!
    }

    fun setCpuCores(cpuCores: String?) {
        this.cpuCores = cpuCores
    }

    fun setAndroidDeviceId(androidDeviceId: String?) {
        this.androidDeviceId = androidDeviceId
    }

    fun getAndroidDeviceId(): String {
        return androidDeviceId!!
    }
}