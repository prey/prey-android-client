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

class EventRetrieveDataNullMobile {
    fun execute(context: Context?, manager: EventManager) {
        val mobileSon = JSONObject()
        try {
            mobileSon.put("mobile_internet", "")
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        manager.receivesData(EventManager.MOBILE, mobileSon)
    }
}