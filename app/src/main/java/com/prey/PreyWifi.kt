/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

class PreyWifi {

    private var name: String? = null
    private var interfaceType: String? = null
    private var model: String? = null
    private var vendor: String? = null
    private var ipAddress: String? = null
    private var gatewayIp: String? = null
    private var netmask: String? = null
    private var macAddress: String? = null
    private var ssid: String? = null
    private var signalStrength: String? = null
    private var channel: String? = null
    private var security: String? = null
    private var wifiEnabled = false

    fun isWifiEnabled(): Boolean {
        return wifiEnabled
    }

    fun setWifiEnabled(wifiEnabled: Boolean) {
        this.wifiEnabled = wifiEnabled
    }

    fun getSsid(): String {
        return ssid!!
    }

    fun setSsid(ssid: String?) {
        this.ssid = ssid
    }

    fun getSignalStrength(): String {
        return signalStrength!!
    }

    fun setSignalStrength(signalStrength: String?) {
        this.signalStrength = signalStrength
    }

    fun getChannel(): String {
        return channel!!
    }

    fun setChannel(channel: String?) {
        this.channel = channel
    }

    fun getSecurity(): String {
        return security!!
    }

    fun setSecurity(security: String?) {
        this.security = security
    }

    fun getName(): String {
        return name!!
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getInterfaceType(): String {
        return interfaceType!!
    }

    fun setInterfaceType(interfaceType: String?) {
        this.interfaceType = interfaceType
    }

    fun getModel(): String {
        return model!!
    }

    fun setModel(model: String?) {
        this.model = model
    }

    fun getVendor(): String {
        return vendor!!
    }

    fun setVendor(vendor: String?) {
        this.vendor = vendor
    }

    fun getIpAddress(): String {
        return ipAddress!!
    }

    fun setIpAddress(ipAddress: String?) {
        this.ipAddress = ipAddress
    }

    fun getGatewayIp(): String {
        return gatewayIp!!
    }

    fun setGatewayIp(gatewayIp: String?) {
        this.gatewayIp = gatewayIp
    }

    fun getNetmask(): String {
        return netmask!!
    }

    fun setNetmask(netmask: String?) {
        this.netmask = netmask
    }

    fun getMacAddress(): String {
        return macAddress!!
    }

    fun setMacAddress(macAddress: String?) {
        this.macAddress = macAddress
    }

}