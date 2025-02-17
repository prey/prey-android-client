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
import com.prey.PreyLogger
import com.prey.PreyPhone

import org.json.JSONObject

/**
 * PrivateIp class is responsible for retrieving and reporting the private IP address.
 */
class PrivateIp : JsonAction() {

    /**
     * Reports the private IP address.
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
    ): MutableList<HttpDataService>? = super.report(context, actionResults, parameters)

    /**
     * Gets the private IP address.
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
    ): MutableList<HttpDataService>? = super.get(context, actionResults, parameters)

    /**
     * Runs the private IP address action.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON parameters.
     * @return The HTTP data service containing the private IP address data.
     */
    override fun run(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? {
        // Get the WiFi instance
        val wifi = PreyPhone.getInstance(context).getWifi()
        // Check if WiFi is available
        if (wifi != null) {
            // Create a new HTTP data service for the private IP address
            val data = HttpDataService("private_ip")
            PreyLogger.d("privateIp:$wifi.getIpAddress()")
            // Set the private IP address data
            data.setSingleData(wifi.getIpAddress())
            // Return the HTTP data service
            return data
        }
        // Return null if WiFi is not available
        return null
    }
}