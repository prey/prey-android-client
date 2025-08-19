/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context

import com.prey.actions.observer.ActionResult
import com.prey.actions.wipe.Wipe
import com.prey.PreyConfig

import org.json.JSONObject

/**
 * Class responsible for handling wipe actions.
 */
class Wipe {

    /**
     * Starts the wipe action.
     *
     * @param context The application context.
     * @param actionResults A list of action results.
     * @param parameters A JSON object containing wipe parameters.
     */
    fun start(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ) {
        execute(context, actionResults, parameters)
    }

    /**
     * Executes the wipe action based on the provided parameters.
     *
     * @param context The application context.
     * @param actionResults A list of action results.
     * @param parameters A JSON object containing wipe parameters.
     */
    fun execute(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ) {
        // Initialize wipe flags
        var shouldWipe = false
        var shouldDeleteSd = false
        // Check if the "parameter" key exists in the parameters JSON object
        if (parameters?.has("parameter") == true) {
            val parameter = parameters.getString("parameter")
            if (parameter == "sd") {
                shouldWipe = false
                shouldDeleteSd = true
            }
        }
        // Get the message ID and job ID from the parameters JSON object
        val messageId = parameters?.getString(PreyConfig.MESSAGE_ID)
        val jobId = parameters?.getString(PreyConfig.JOB_ID)
        val factoryReset = parameters?.getString("factory_reset")
        if (factoryReset == "on" || factoryReset == "y" || factoryReset == "true") {
            shouldWipe = true
        } else if (factoryReset == "off" || factoryReset == "n" || factoryReset == "false") {
            shouldWipe = false
        }
        val wipeSim = parameters?.getString("wipe_sim")
        if (wipeSim == "on" || wipeSim == "y" || wipeSim == "true") {
            shouldDeleteSd = true
        } else if (wipeSim == "off" || wipeSim == "n" || wipeSim == "false") {
            shouldDeleteSd = false
        }
        // Start the wipe thread with the determined flags
        Wipe(context, shouldWipe, shouldDeleteSd, messageId!!, jobId).start()
    }

}