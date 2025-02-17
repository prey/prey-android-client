/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

import com.prey.PreyLogger

class PreyWifiManager {

    fun isWifiEnabled(context: Context): Boolean {
        val wifiMgr = context.getSystemService(android.content.Context.WIFI_SERVICE) as WifiManager
        return wifiMgr.isWifiEnabled ?: false
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }

    private fun getConnectionInfo(context: Context): WifiInfo? {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.connectionInfo
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return null
    }

    fun getSSID(context: Context): String? {
        val connectionInfo = getConnectionInfo(context) ?: return null
        return connectionInfo.ssid?.replace("\"", "")
    }

    companion object {
        private var instance: PreyWifiManager? = null
        fun getInstance(): PreyWifiManager {
            return instance ?: PreyWifiManager().also { instance = it }
        }
    }
}