/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.events.Event
import com.prey.events.manager.EventManager
import com.prey.json.CommandTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Action class responsible for processing ping-related commands.
 *
 * This class handles requests to retrieve and report the current device status
 * by creating a [Event.DEVICE_STATUS] event and passing it to the [EventManager].
 */
class Ping : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    /**
     * Executes the specified command for the Ping action.
     *
     * @param context The application context.
     * @param command The command to be executed (e.g., [CMD_GET]).
     * @param options A [JSONObject] containing any parameters required for the command.
     * @throws IllegalArgumentException if the provided command is not supported.
     */
    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_GET -> scope.launch { get(context, options) }
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Processes a request to retrieve and report the current status of the device.
     *
     * @param context The application context.
     * @param options A [JSONObject] containing additional parameters for the status event.
     */
    suspend fun get(context: Context, options: JSONObject) {
        val eventStatus = Event(Event.DEVICE_STATUS, options.toString())
        EventManager.process(context, eventStatus)
    }

}