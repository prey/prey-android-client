/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager.kotlin

import android.content.Context
import com.prey.events.kotlin.Event
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices
import org.json.JSONObject
import java.net.HttpURLConnection

class EventThread : Thread {
    private var jsonObjectStatus: JSONObject

    private var event: Event
    private var ctx: Context
    private var eventGeo: String?

    constructor(ctx: Context, event: Event, jsonObjectStatus: JSONObject) {
        this.ctx = ctx
        this.event = event
        this.jsonObjectStatus = jsonObjectStatus
        this.eventGeo = null
    }

    constructor(ctx: Context, event: Event, jsonObjectStatus: JSONObject, eventGeo: String?) {
        this.ctx = ctx
        this.event = event
        this.jsonObjectStatus = jsonObjectStatus
        this.eventGeo = eventGeo
    }

    override fun run() {
        try {
            val valida = EventControl.getInstance().valida(jsonObjectStatus)
            PreyLogger.d("EVENT valida:" + valida + " eventName:" + event.name)
            if (valida) {
                val preyHttpResponse =
                    PreyWebServices.getInstance().sendPreyHttpEvent(ctx, event, jsonObjectStatus)
                if (preyHttpResponse != null) {
                    if (preyHttpResponse.getStatusCode() == HttpURLConnection.HTTP_OK && eventGeo != null) {
                        PreyLogger.d("EVENT sendPreyHttpEvent eventName:$eventGeo")
                    }
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("EVENT Error EventThread:" + e.message, e)
        }
    }
}