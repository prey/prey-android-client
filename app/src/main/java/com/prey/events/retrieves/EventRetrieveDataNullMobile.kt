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
 * EventRetrieveDataNullMobile is responsible for retrieving and processing null mobile internet data.
 */
class EventRetrieveDataNullMobile {

    /**
     * Executes the retrieval of null mobile internet data.
     *
     * @param context The application context.
     * @param manager The EventManager instance.
     */
    fun execute(context: Context?, manager: EventManager) {
        val mobileSon = JSONObject()
        try {
            mobileSon.put("mobile_internet", "")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        // Send the mobile internet data to the EventManager
        manager.receivesData(EventManager.MOBILE, mobileSon)
    }
}