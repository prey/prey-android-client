/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.PreyLogger
import com.prey.PreyPhone

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
            val phone = PreyPhone.getInstance(ctx)
            val wifiPhone = phone.getWifi()!!
            val parametersMap = HashMap<String, String?>()
            parametersMap.put(SSID, wifiPhone.getSsid());
            parametersMap.put("mac_address", wifiPhone.getMacAddress());
            parametersMap.put("security", wifiPhone.getSecurity());
            parametersMap.put("signal_strength", wifiPhone.getSignalStrength());
            parametersMap.put("channel", wifiPhone.getChannel());
            parametersMap.put("interfaceType", wifiPhone.getInterfaceType());
            parametersMap.put("model", wifiPhone.getModel());
            parametersMap.put("vendor", wifiPhone.getVendor());
            parametersMap.put("ipAddress", wifiPhone.getIpAddress());
            parametersMap.put("gatewayIp", wifiPhone.getGatewayIp());
            parametersMap.put("netmask", wifiPhone.getNetmask());
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