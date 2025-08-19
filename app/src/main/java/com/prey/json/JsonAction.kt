/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json

import android.content.Context
import com.prey.PreyConfig

import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.PreyLogger

import org.json.JSONObject

/**
 * Abstract class representing a JSON action.
 * Provides methods for reporting and getting data.
 */
abstract class JsonAction {

    /**
     * Reports data to the server.
     *
     * @param context The application context.
     * @param actionResults A list of action results.
     * @param parameters A JSON object containing parameters.
     * @return A list of HTTP data services.
     */
    open fun report(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val dataToBeSent: MutableList<HttpDataService> = ArrayList()
        try {
            val data = run(context, actionResults, parameters)
            if (data != null) {
                actionResults?.add(ActionResult().apply { this.dataToSend = data })
                dataToBeSent.add(data)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error causa:${e.message}", e)
        }
        return dataToBeSent
    }

    /**
     * Gets data from the server.
     *
     * @param context The application context.
     * @param actionResults A list of action results.
     * @param parameters A JSON object containing parameters.
     * @return A list of HTTP data services.
     */
    open fun get(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val data = run(context, actionResults, parameters)
        return if (data != null) {
            val dataToSend = ArrayList<HttpDataService>()
            dataToSend.add(data)
            PreyConfig.getInstance(context).getWebServices().sendPreyHttpData(context, dataToSend)
            dataToSend
        } else {
            null
        }
    }

    /**
     * Abstract method to run the action.
     *
     * @param context The application context.
     * @param actionResults A list of action results.
     * @param parameters A JSON object containing parameters.
     * @return An HTTP data service.
     */
    abstract fun run(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService?

}