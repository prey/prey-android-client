/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves.kotlin

import android.content.Context
import com.prey.events.manager.kotlin.EventManager
import com.prey.kotlin.PreyLogger
import org.json.JSONObject

class EventRetrieveDataOnline {
    fun execute(context: Context?, manager: EventManager) {
        val onlineJSon = JSONObject()
        try {
            onlineJSon.put("online", true)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        PreyLogger.d("online:true")
        manager.receivesData(EventManager.ONLINE, onlineJSon)
    }
}