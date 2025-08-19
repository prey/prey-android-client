/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager

import android.content.Context
import com.prey.PreyConfig

import com.prey.events.Event
import com.prey.PreyLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.json.JSONObject
import java.net.HttpURLConnection

/**
 * EventSend class responsible for handling events and sending data to the server.
 */
class EventSend {
    // Properties to store the context, event, status JSON, and geo event
    private lateinit var statusJson: JSONObject
    private lateinit var event: Event
    private lateinit var context: Context
    private var geoEvent: String? = null

    /**
     * Primary constructor for EventSend.
     *
     * @param context The application context.
     * @param event The event to be handled.
     * @param statusJson The JSON object containing the event status.
     */
    constructor(context: Context, event: Event, statusJson: JSONObject) : this(
        context,
        event,
        statusJson,
        null
    )

    /**
     * Secondary constructor for EventSend with an optional geo event.
     *
     * @param context The application context.
     * @param event The event to be handled.
     * @param statusJson The JSON object containing the event status.
     * @param geoEvent The geo event associated with the event (optional).
     */
    constructor(context: Context, event: Event, statusJson: JSONObject, geoEvent: String?) {
        this.context = context
        this.event = event
        this.statusJson = statusJson
        this.geoEvent = geoEvent
    }

    /**
     * Runs the event, validating the event and sending the event data to the server if valid.
     */
    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Validate the event using the EventControl instance
                val isValid = EventControl.getInstance().isValid(statusJson)
                PreyLogger.d("EVENT isValid:${isValid} eventName:${event.name}")
                if (isValid) {
                    val preyHttpResponse =
                        PreyConfig.getInstance(context).getWebServices()
                            .sendPreyHttpEvent(context, event, statusJson)
                    if (preyHttpResponse != null) {
                        if (preyHttpResponse.getStatusCode() == HttpURLConnection.HTTP_OK && geoEvent != null) {
                            PreyLogger.d("EVENT sendPreyHttpEvent eventName:$geoEvent")
                        }
                    }
                }
            } catch (e: Exception) {
                PreyLogger.e("EVENT Error EventSend:${e.message}", e)
            }
        }
    }

}