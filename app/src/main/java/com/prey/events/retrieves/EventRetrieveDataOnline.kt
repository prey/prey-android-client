/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves

import android.content.Context

import com.prey.events.manager.EventManager
import com.prey.PreyLogger

import org.json.JSONObject

/**
 * EventRetrieveDataOnline is responsible for retrieving and processing online data.
 */
class EventRetrieveDataOnline {

    /**
     * Execute the online data retrieval and send the result to the EventManager.
     *
     * @param context The application context.
     * @param manager The EventManager instance.
     */
    fun execute(context: Context?, manager: EventManager) {
        val onlineJSon = JSONObject()
        try {
            onlineJSon.put("online", true)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}" , e)
        }
        // Send the online data to the EventManager
        manager.receivesData(EventManager.ONLINE, onlineJSon)
    }
}