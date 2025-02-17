/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * This class provides methods to manage and retrieve information about the device's connectivity.
 */
class PreyConnectivityManager {

    /**
     * Returns the ConnectivityManager instance for the given context.
     *
     * @param context The application context.
     * @return The ConnectivityManager instance.
     */
    private fun getConnectivityManager(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    /**
     * Returns the active network information for the given ConnectivityManager instance.
     *
     * @param connectivityManager The ConnectivityManager instance.
     * @return The active network information, or null if not available.
     */
    private fun getActiveNetworkInfo(connectivityManager: ConnectivityManager): NetworkInfo? {
        return connectivityManager.activeNetworkInfo
    }

    /**
     * Returns the network information for the given network type and ConnectivityManager instance.
     *
     * @param connectivityManager The ConnectivityManager instance.
     * @param networkType The network type (e.g. TYPE_MOBILE, TYPE_WIFI).
     * @return The network information, or null if not available.
     */
    private fun getNetworkInfo(connectivityManager: ConnectivityManager, networkType: Int): NetworkInfo? {
        return connectivityManager.getNetworkInfo(networkType)
    }

    /**
     * Checks if the device is connected to a network.
     *
     * @param context The application context.
     * @return True if the device is connected, false otherwise.
     */
    fun isConnected(context: Context): Boolean {
        val activeNetworkInfo = getActiveNetworkInfo(getConnectivityManager(context))
        return activeNetworkInfo?.isConnected ?: false
    }

    /**
     * Checks if the device is connected to a mobile network.
     *
     * @param context The application context.
     * @return True if the device is connected to a mobile network, false otherwise.
     */
    fun isMobileConnected(context: Context): Boolean {
        val mobileNetworkInfo = getNetworkInfo(getConnectivityManager(context), ConnectivityManager.TYPE_MOBILE)
        return mobileNetworkInfo?.isConnected ?: false
    }

    /**
     * Checks if the device is connected to a Wi-Fi network.
     *
     * @param context The application context.
     * @return True if the device is connected to a Wi-Fi network, false otherwise.
     */
    fun isWifiConnected(context: Context): Boolean {
        val wifiNetworkInfo = getNetworkInfo(getConnectivityManager(context), ConnectivityManager.TYPE_WIFI)
        return wifiNetworkInfo?.isAvailable ?: false
    }

    companion object {
        private var instance: PreyConnectivityManager? = null
        fun getInstance(): PreyConnectivityManager {
            return instance ?: synchronized(this) {
                instance ?: PreyConnectivityManager().also { instance = it }
            }
        }
    }
}