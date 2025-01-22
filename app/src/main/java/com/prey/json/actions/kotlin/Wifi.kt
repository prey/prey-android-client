/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.content.Context
import com.prey.actions.kotlin.HttpDataService
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPhone

import org.json.JSONObject

class Wifi {
    fun run(
        ctx: Context,
        list: List<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService {
        val data = HttpDataService("wifi")
        try {
            data.setList(true)
            val phone = PreyPhone(ctx)
            val wifiPhone = phone.wifi!!
            val parametersMap = HashMap<String, String?>()
            parametersMap[SSID] = wifiPhone.ssid!!
            parametersMap["mac_address"] = wifiPhone.macAddress!!
            parametersMap["security"] = wifiPhone.security!!
            parametersMap["signal_strength"] = wifiPhone.signalStrength!!
            parametersMap["channel"] = wifiPhone.channel!!
            parametersMap["interfaceType"] = wifiPhone.interfaceType!!
            parametersMap["model"] = wifiPhone.model!!
            parametersMap["vendor"] = wifiPhone.vendor!!
            parametersMap["ipAddress"] = wifiPhone.ipAddress!!
            parametersMap["gatewayIp"] = wifiPhone.gatewayIp!!
            parametersMap["netmask"] = wifiPhone.netmask!!
            data.addDataListAll(parametersMap)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message + e.message, e)
        }
        return data
    }

    companion object {
        var SSID: String = "ssid"
    }
}