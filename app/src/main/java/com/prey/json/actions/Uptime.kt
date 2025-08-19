/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.os.SystemClock

import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.json.JsonAction

import org.json.JSONObject

/**
 * This class represents an action to retrieve the device's uptime.
 */
class Uptime : JsonAction() {

    /**
     * Retrieves the uptime data.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON parameters.
     * @return The list of HTTP data services containing the uptime data.
     */
    override fun get(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? = super.get(context, actionResults, parameters)

    /**
     * Runs the uptime action and returns the uptime data as an HTTP data service.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON parameters.
     * @return The HTTP data service containing the uptime data.
     */
    override fun run(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService {
        // Get the device's uptime in milliseconds
        val uptime = SystemClock.uptimeMillis()
        // Create a new HTTP data service for the uptime data
        val data = HttpDataService("uptime")
        // Set the uptime data as a single value
        data.setSingleData(uptime.toString())
        // Return the HTTP data service
        return data
    }

}