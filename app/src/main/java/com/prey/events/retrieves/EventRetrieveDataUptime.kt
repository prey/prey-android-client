/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves

import android.content.Context

import com.prey.events.manager.EventManager
import com.prey.json.actions.Uptime
import com.prey.PreyLogger

import org.json.JSONObject

/**
 * EventRetrieveDataUptime is responsible for retrieving and processing uptime data.
 */
class EventRetrieveDataUptime {

    /**
     * Executes the retrieval of uptime data.
     *
     * @param context The application context.
     * @param manager The EventManager instance.
     */
    fun execute(context: Context, manager: EventManager) {
        try {
            val uptimeHttpDataService = Uptime().run(context, null, null)
            val uptimeData = uptimeHttpDataService!!.getSingleData()
            val uptimeJSon = JSONObject()
            uptimeJSon.put("uptime", uptimeData)
            PreyLogger.d("uptime:$uptimeData")
            // Send the uptime data to the EventManager
            manager.receivesData(EventManager.UPTIME, uptimeJSon)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }
}