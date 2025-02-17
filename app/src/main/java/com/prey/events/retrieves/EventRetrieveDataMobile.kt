/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves

import android.content.Context
import android.net.ConnectivityManager

import com.prey.events.manager.EventManager
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPhone

import org.json.JSONObject

/**
 * EventRetrieveDataMobile is responsible for retrieving and processing mobile internet data.
 */
class EventRetrieveDataMobile {

    /**
     * Executes the retrieval of mobile internet data.
     *
     * @param context The application context.
     * @param manager The EventManager instance.
     */
    fun execute(context: Context, manager: EventManager) {
        val mobileSon = JSONObject()
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            var mobile_internet: String? = null
            if (activeNetwork != null && activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                try {
                    mobile_internet = PreyPhone.getInstance(context).getNetworkClass(context)
                    if (mobile_internet != null) {
                        PreyConfig.getInstance(context).setPreviousSsid(mobile_internet)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("Error:${e.message}", e)
                }
            }
            mobileSon.put("mobile_internet", mobile_internet)
            PreyLogger.d("mobile_internet:$mobile_internet")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        // Send the mobile internet data to the EventManager
        manager.receivesData(EventManager.MOBILE, mobileSon)
    }
}