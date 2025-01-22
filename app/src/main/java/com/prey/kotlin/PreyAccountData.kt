/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.kotlin

class PreyAccountData {
    private var apiKey: String? = null
    private var deviceId: String? = null
    private var name: String? = null
    private var password: String? = null
    private var email: String ? = null
    private var refererId: String? = null
    private var preyVersion: String? = null
    private var isMissing: Boolean = false

    fun getApiKey(): String {
        return apiKey!!
    }

    fun setApiKey(apiKey: String?) {
        this.apiKey = apiKey
    }

    fun getDeviceId(): String {
        return deviceId!!
    }

    fun setDeviceId(deviceId: String?) {
        this.deviceId = deviceId
    }

    fun getPassword(): String {
        return password!!
    }

    fun setPassword(password: String?) {
        this.password = password
    }

    fun getEmail(): String {
        return email!!
    }

    fun setEmail(email: String?) {
        this.email = email
    }

    fun getRefererId(): String {
        return refererId!!
    }

    fun setRefererId(refererId: String?) {
        this.refererId = refererId
    }

    fun getName(): String {
        return name!!
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getPreyVersion(): String {
        return preyVersion!!
    }

    fun setPreyVersion(preyVersion: String?) {
        this.preyVersion = preyVersion
    }

    fun setMissing(missing: Boolean) {
        this.isMissing = missing
    }

    fun isMissing(): Boolean {
        return isMissing
    }
}