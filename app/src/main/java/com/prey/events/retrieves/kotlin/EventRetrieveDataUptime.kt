/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves.kotlin

import android.content.Context
import com.prey.events.manager.kotlin.EventManager
import com.prey.json.actions.kotlin.Uptime
import com.prey.kotlin.PreyLogger
import org.json.JSONObject

class EventRetrieveDataUptime {
    fun execute(context: Context, manager: EventManager) {
        try {
            val uptimeHttpDataService = Uptime().run(context, null, null)
            val uptimeData = uptimeHttpDataService!!.getSingleData()
            val uptimeJSon = JSONObject()
            uptimeJSon.put("uptime", uptimeData)
            PreyLogger.d("uptime:$uptimeData")
            manager.receivesData(EventManager.UPTIME, uptimeJSon)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }
}