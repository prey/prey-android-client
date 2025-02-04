/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers

import android.content.Context
import android.net.ConnectivityManager
import com.prey.PreyLogger

class PreyConnectivityManager {

    fun isConnected(ctx: Context): Boolean {
        val connectivity = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity!!.activeNetworkInfo != null) return connectivity.activeNetworkInfo!!
            .isConnected
        return false
    }

    fun isAvailable(ctx: Context): Boolean {
        val connectivity = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity!!.activeNetworkInfo != null) return connectivity.activeNetworkInfo!!
            .isAvailable
        return false
    }

    fun isConnectedOrConnecting(ctx: Context): Boolean {
        val connectivity = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity!!.activeNetworkInfo != null) return connectivity.activeNetworkInfo!!
            .isConnectedOrConnecting
        return false
    }

    fun isFailover(ctx: Context): Boolean {
        val connectivity = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity!!.activeNetworkInfo != null) return connectivity.activeNetworkInfo!!
            .isFailover
        return false
    }

    fun isRoaming(ctx: Context): Boolean {
        val connectivity = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity!!.activeNetworkInfo != null) return connectivity.activeNetworkInfo!!
            .isRoaming
        return false
    }

    fun isMobileAvailable(ctx: Context): Boolean {
        try {
            val connectivity =
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mobile = connectivity!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            return mobile!!.isAvailable
        } catch (e: Exception) {
            return false
        }
    }

    fun isMobileConnected(ctx: Context): Boolean {
        try {
            val connectivity =
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mobile = connectivity!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            return mobile!!.isConnected
        } catch (e: Exception) {
            return false
        }
    }

    fun isWifiAvailable(ctx: Context): Boolean {
        val connectivity = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = connectivity!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return wifi!!.isAvailable
    }

    fun isWifiConnected(ctx: Context): Boolean {
        try {
            val connectivity =
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifi = connectivity!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return wifi!!.isConnected
        } catch (e: Exception) {
            PreyLogger.d("Error isWifiConnected:" + e.message)
            return false
        }
    }

    companion object {
        private var INSTANCE: PreyConnectivityManager? = null
        fun getInstance(): PreyConnectivityManager {
            if (INSTANCE == null) {
                INSTANCE = PreyConnectivityManager()
            }
            return INSTANCE!!
        }
    }
}