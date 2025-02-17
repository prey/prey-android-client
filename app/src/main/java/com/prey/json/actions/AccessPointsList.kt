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
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPhone
import com.prey.managers.PreyConnectivityManager
import com.prey.net.PreyWebServices

import org.json.JSONObject

/**
 * AccessPointsList class is responsible for retrieving and reporting access points list.
 */
class AccessPointsList : JsonAction() {

    /**
     * Reports the access points list.
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
     * Gets the access points list.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON parameters.
     * @return The list of HTTP data services.
     */
    override fun get(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        return super.get(context, actionResults, parameters)
    }

    /**
     * Runs the access points list action.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON parameters.
     * @return The HTTP data service.
     */
    override fun run(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService {
        // Create a new HTTP data service for access points list
        val wifiData = HttpDataService("access_points_list")
        // Try to get the message ID from the parameters
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        // Try to retrieve the access points list
        try {
            if (PreyConnectivityManager.getInstance().isWifiConnected(context)) {
                // Create a new hash map to store the Wi-Fi parameters
                val wifiParameters = HashMap<String, String?>()
                // Get the phone instance
                val phone = PreyPhone.getInstance(context)
                // Get the list of Wi-Fi access points
                val wifiList = phone.getListWifi()
                // Check if the list is not null
                if (wifiList != null) {
                    // Iterate over the list of access points
                    for ((index, wifi) in wifiList.withIndex()) {
                        wifiParameters["${index}][ssid"] = wifi.getSsid()
                        wifiParameters["${index}][mac_address"] = wifi.getMacAddress()
                        wifiParameters["${index}][security"] = wifi.getSecurity()
                        wifiParameters["${index}][signal_strength"] = wifi.getSignalStrength()
                        wifiParameters["${index}][channel"] = wifi.getChannel()
                    }
                    // Set the list flag to true
                    wifiData.setList(true)
                    // Add the Wi-Fi parameters to the data list
                    wifiData.getDataList().putAll(wifiParameters)
                }
            }
        } catch (e: Exception) {
            // Send a notification to the server if an error occurs
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("get", "access_points_list", "failed", e.message)
            )
            PreyLogger.e("Error causa:" + e.message + e.message, e)
        }
        return wifiData
    }
}