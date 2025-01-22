/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers.kotlin

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.prey.kotlin.PreyLogger

class PreyWifiManager {

    fun isWifiEnabled(ctx: Context): Boolean {
        val wifiMgr = ctx.getSystemService(android.content.Context.WIFI_SERVICE) as WifiManager
        return wifiMgr.isWifiEnabled ?: false
    }

    fun isOnline(ctx: Context): Boolean {
        val cm = ctx!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnectedOrConnecting) {
            return true
        }
        return false
    }

    fun connectionInfo(ctx: Context): WifiInfo? {
        try {
            val wifiMgr = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wifiMgr != null) return wifiMgr.connectionInfo
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return null
    }

    fun sSID(ctx: Context): String? {
        if (connectionInfo(ctx) != null) {
            var ssid = connectionInfo(ctx)!!.ssid
            if (ssid != null) {
                ssid = ssid.replace("\"", "")
            }
            return ssid
        }
        return null
    }

    companion object {
        private var INSTANCE: PreyWifiManager? = null
        fun getInstance(): PreyWifiManager {
            if (PreyWifiManager.INSTANCE == null) {
                PreyWifiManager.INSTANCE = PreyWifiManager()
            }
            return PreyWifiManager.INSTANCE!!
        }
    }
}