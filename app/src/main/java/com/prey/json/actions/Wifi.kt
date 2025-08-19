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

/**
 * This class represents a Wifi action that retrieves and reports wifi-related data.
 */
class Wifi {

    /**
     * Runs the wifi action and returns the collected data as an HttpDataService.
     *
     * @param context The application context.
     * @param actionResults The list of action results.
     * @param parameters The JSON parameters.
     * @return The HttpDataService containing the wifi data.
     */
    fun run(
        context: Context,
        actionResults: List<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService {
        // Create a new HttpDataService instance for wifi data
        val data = HttpDataService("wifi")
        try {
            data.setList(true)
            // Get the phone instance and its wifi information
            val phone = PreyPhone.getInstance(context)
            val wifiPhone = phone.getWifi()!!
            val parametersMap = HashMap<String, String?>()
            parametersMap[SSID] = wifiPhone.getSsid()
            parametersMap["mac_address"] = wifiPhone.getMacAddress()
            parametersMap["security"] = wifiPhone.getSecurity()
            parametersMap["signal_strength"] = wifiPhone.getSignalStrength()
            parametersMap["channel"] = wifiPhone.getChannel()
            parametersMap["interfaceType"] = wifiPhone.getInterfaceType()
            parametersMap["model"] = wifiPhone.getModel()
            parametersMap["vendor"] = wifiPhone.getVendor()
            parametersMap["ipAddress"] = wifiPhone.getIpAddress()
            parametersMap["gatewayIp"] = wifiPhone.getGatewayIp()
            parametersMap["netmask"] = wifiPhone.getNetmask()
            // Add the parameters map to the HttpDataService
            data.addDataListAll(parametersMap)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        // Return the HttpDataService containing the wifi data
        return data
    }

    companion object {
        var SSID: String = "ssid"
    }

}