/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.observer

import android.content.Context

import com.prey.actions.HttpDataService
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.location.PreyLocationManager
import com.prey.util.ClassUtil

import org.json.JSONObject

/**
 * Controller class responsible for executing actions based on JSON objects.
 */
class ActionsController {

    /**
     * Executes a list of JSON actions and returns the resulting HTTP data services.
     *
     * @param context     The application context.
     * @param jsonObjects The list of JSON objects containing action commands.
     * @return The list of HTTP data services resulting from the action execution.
     */
    fun runActionJson(context: Context, jsonObjects: List<JSONObject>): List<HttpDataService>? {
        // Initialize an empty list to store the resulting HTTP data services.
        var data = mutableListOf<HttpDataService>()
        // Get the size of the JSON object list.
        val size = jsonObjects.size ?: -1
        PreyLogger.d("AWARE runActionJson size:${size}")
        try {
            // Reset the last location and location info.
            PreyLocationManager.getInstance().setLastLocation(null);
            PreyConfig.getInstance(context).setLocation(null);
            PreyConfig.getInstance(context).setLocationInfo("");
            // Iterate through each JSON object in the list.
            jsonObjects.forEach { jsonObject ->
                PreyLogger.d("jsonObject:${jsonObject}")
                // Get the command object from the JSON object, defaulting to the JSON object itself if not found.
                val command = jsonObject.optJSONObject("cmd") ?: jsonObject
                // Extract the action name, method, and parameters from the command object.
                val actionName = command.getString("target")
                val actionMethod = command.getString("command")
                val actionParameters = command.optJSONObject("options") ?: JSONObject()
                // Add the message ID to the action parameters if present.
                actionParameters.put(
                    PreyConfig.MESSAGE_ID,
                    command.optString(PreyConfig.MESSAGE_ID)
                )
                // Log the action details for debugging purposes.
                PreyLogger.d("actionName:${actionName} actionMethod:${actionMethod} actionParameters:${actionParameters}")
                // Initialize an empty list to store the action results.
                val actionResults = mutableListOf<ActionResult>()
                // Execute the action using the ClassUtil class and update the data list with the result.
                val resultData = ClassUtil.getInstance().execute(
                    context,
                    actionResults,
                    actionName,
                    actionMethod,
                    actionParameters,
                    data
                )
                if (resultData != null) {
                    data = resultData
                }
            }
            return data
        } catch (e: Exception) {
            PreyLogger.e("Error, cause:${e.message}", e)
        }
        return null
    }

    companion object {
        private var instance: ActionsController? = null

        /**
         * Returns the singleton instance of the ActionsController class.
         *
         * @return The singleton instance of the ActionsController class.
         */
        fun getInstance(): ActionsController {
            return instance ?: ActionsController().also { instance = it }
        }
    }

}