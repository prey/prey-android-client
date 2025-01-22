/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves.kotlin

import android.content.Context
import com.prey.events.manager.kotlin.EventManager
import com.prey.json.actions.kotlin.PrivateIp
import com.prey.kotlin.PreyLogger
import org.json.JSONObject

class EventRetrieveDataPrivateIp {
    fun execute(context: Context, manager: EventManager) {
        try {
            val privateIpHttpDataService = PrivateIp().run(context, null, null)
            val privateIpData = privateIpHttpDataService!!.getSingleData()
            val privateIpJSon = JSONObject()
            privateIpJSon.put("private_ip", privateIpData)
            PreyLogger.d("privateIp:$privateIpData")
            manager.receivesData(EventManager.PRIVATE_IP, privateIpJSon)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }
}