/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.PreyLogger
import com.prey.actions.triggers.TriggerController
import com.prey.json.CommandTarget
import org.json.JSONObject

/**
 * Manages the execution of trigger-related commands received from the Prey panel.
 *
 * This class acts as a command target, routing "start" and "stop" commands
 * to the appropriate methods for handling Prey's trigger system. Triggers are
 * automated actions based on specific conditions (e.g., entering or leaving a geofence).
 *
 * @see CommandTarget
 * @see TriggerController
 */
class Triggers : CommandTarget, BaseAction() {

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_GET -> start(context, options)
            CMD_STOP -> stop(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Starts the trigger evaluation process.
     *
     * This function initiates the trigger mechanism, which checks for and executes predefined actions
     * based on certain conditions. It logs the provided options for debugging purposes and then
     * invokes the `TriggerController` to begin its run cycle. A short delay is introduced before
     * starting the controller.
     *
     * @param context The application context, used by the `TriggerController` to access system services.
     * @param options A `JSONObject` containing configuration or parameters for the start command.
     *                Currently, these options are logged but not used directly in this function.
     */
    fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Triggers start options:${options}")
        Thread.sleep(2000)
        TriggerController.getInstance().run(context)
    }

    fun stop(context: Context, options: JSONObject) {
        PreyLogger.d("Triggers stop options:${options}")
        TriggerController.getInstance().run(context);
    }

}