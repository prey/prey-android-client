/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves

import android.content.Context

import com.prey.events.Event
import com.prey.events.manager.EventManager
import com.prey.json.actions.Wifi
import com.prey.PreyConfig
import com.prey.PreyLogger

import org.json.JSONObject

/**
 * Class responsible for retrieving and processing WiFi data.
 */
class EventRetrieveDataWifi {

    /**
     * Executes the WiFi data retrieval and processing.
     *
     * @param context The application context.
     * @param manager The EventManager instance.
     */
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
            if (ssid != null) {
                PreyConfig.getInstance(context).setPreviousSsid(ssid)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyLogger.d("wifi:$ssid")
        // Send the WiFi data to the EventManager
        manager.receivesData(EventManager.WIFI, wifiJSon)
    }
}