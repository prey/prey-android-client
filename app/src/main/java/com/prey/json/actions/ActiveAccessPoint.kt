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
import com.prey.json.JsonAction
import com.prey.PreyPhone

import org.json.JSONObject

/**
 * ActiveAccessPoint class is responsible for retrieving and reporting the active access point.
 */
class ActiveAccessPoint : JsonAction() {

    /**
     * Reports the active access point.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON parameters.
     * @return The list of HTTP data services.
     */
    override fun report(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        return super.report(context, actionResults, parameters)
    }

    /**
     * Runs the active access point action.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON parameters.
     * @return The HTTP data service containing the active access point data.
     */
    override fun run(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService {
        // Create a new HTTP data service for the active access point
        val activeAccessPointData = HttpDataService("active_access_point")
        activeAccessPointData.setList(true)
        val phone = PreyPhone.getInstance(context)
        val wifi = phone.getWifi()
        // Check if WiFi is enabled and available
        if (wifi != null && wifi.isWifiEnabled()) {
            val ssid = wifi.getSsid()
            // Check if the SSID is valid
            if (ssid != null && ssid.isNotEmpty() && ssid != "<unknown ssid>") {
                val wifiInfo = HashMap<String, String?>()
                // Add the WiFi information to the hash map
                wifiInfo["ssid"] = ssid
                wifiInfo["security"] = wifi.getSecurity()
                wifiInfo["mac_address"] = wifi.getMacAddress()
                wifiInfo["signal_strength"] = wifi.getSignalStrength()
                wifiInfo["channel"] = wifi.getChannel()
                // Add the WiFi information to the HTTP data service
                activeAccessPointData.addDataListAll(wifiInfo)
            }
        }
        // Return the HTTP data service containing the active access point data
        return activeAccessPointData
    }
}