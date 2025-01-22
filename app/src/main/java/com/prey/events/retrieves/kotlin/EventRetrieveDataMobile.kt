/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves.kotlin

import android.content.Context
import android.net.ConnectivityManager
import com.prey.events.manager.kotlin.EventManager
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPhone
import org.json.JSONObject

class EventRetrieveDataMobile {
    fun execute(context: Context, manager: EventManager) {
        val mobileSon = JSONObject()
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            var mobile_internet: String? = null
            if (activeNetwork != null && activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                try {
                    mobile_internet = PreyPhone.getInstance(context)!!.getNetworkClass(context)
                    PreyConfig.getInstance(context).setPreviousSsid(mobile_internet)
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            mobileSon.put("mobile_internet", mobile_internet)
            PreyLogger.d("mobile_internet:$mobile_internet")
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        manager.receivesData(EventManager.MOBILE, mobileSon)
    }
}