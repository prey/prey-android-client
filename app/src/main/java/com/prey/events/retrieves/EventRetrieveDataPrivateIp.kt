/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves

import android.content.Context

import com.prey.events.manager.EventManager
import com.prey.json.actions.PrivateIp
import com.prey.PreyLogger

import org.json.JSONObject

/**
 * EventRetrieveDataPrivateIp is responsible for retrieving and processing the private IP address.
 */
class EventRetrieveDataPrivateIp {

    /**
     * Executes the retrieval of the private IP address and sends it to the manager.
     *
     * @param context The application context.
     * @param manager The event manager.
     */
    fun execute(context: Context, manager: EventManager) {
        try {
            val privateIpHttpDataService = PrivateIp().run(context, null, null)
            val privateIpData = privateIpHttpDataService!!.getSingleData()
            val privateIpJSon = JSONObject()
            privateIpJSon.put("private_ip", privateIpData)
            PreyLogger.d("privateIp:$privateIpData")
            manager.receivesData(EventManager.PRIVATE_IP, privateIpJSon)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }
}