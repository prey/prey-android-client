/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves.kotlin

import android.content.Context
import com.prey.events.kotlin.Event
import com.prey.events.manager.kotlin.EventManager
import com.prey.json.actions.kotlin.Wifi
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import org.json.JSONObject

class EventRetrieveDataWifi {
    fun execute(context: Context, manager: EventManager) {
        val wifiHttpDataService = Wifi().run(context, null, null)
        var wifiMapData: MutableMap<String, String?> = wifiHttpDataService.getDataList()!!
        val wifiJSon = JSONObject()
        var ssid: String? = null
        try {
            ssid = wifiMapData[Wifi.SSID]
            val accessElementJSon = JSONObject()
            accessElementJSon.put("ssid", ssid)
            accessElementJSon.put("signal_strength", wifiMapData["signal_strength"])
            accessElementJSon.put("channel", wifiMapData["channel"])
            accessElementJSon.put("security", wifiMapData["security"])
            if (Event.WIFI_CHANGED == manager.event!!.name) {
                manager.event!!.info = ssid
            }
            wifiJSon.put("active_access_point", accessElementJSon)
            PreyConfig.getInstance(context).setPreviousSsid(ssid)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        PreyLogger.d("wifi:$ssid")
        manager.receivesData(EventManager.WIFI, wifiJSon)
    }
}