/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

/**
 * Utility object that provides helper methods for retrieving network-related information and connectivity status.
 *
 * This object contains functions to scan for available Wi-Fi access points, retrieve current connection
 * details, identify the active network transport type (Wi-Fi, Cellular, or Ethernet), and determine
 * cellular data generations (e.g., 2G, 3G, 4G, 5G). It handles Android version compatibility and
 * encapsulates the necessary permission checks required for accessing connectivity and telephony services.
 */
object PreyPhoneKt {

    /**
     * Scans and retrieves a list of nearby Wi-Fi access points.
     *
     * This function checks for the necessary location permissions required to perform a
     * Wi-Fi scan on Android. If permissions are granted, it attempts to retrieve the
     * latest scan results from the [WifiManager].
     *
     * @param context The application context used to check permissions and access system services.
     * @return A list of [ScanResult] containing information about nearby access points.
     *         Returns an empty list if permissions are missing or the Wi-Fi service is unavailable.
     */
    fun getListWifi(context: Context): List<ScanResult> {
        //Cleaner permit verification
        val hasLocationPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasLocationPermission) {
            PreyLogger.d("Location permission not granted to scan WiFi")
            return emptyList()
        }
        //Secure service acquisition
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        PreyLogger.d("getListWifi scanResults:${wifiMgr?.scanResults }")
        //Handling nulls and defensive return
        return wifiMgr?.scanResults ?: emptyList()
    }

    /**
     * Retrieves information about the currently connected Wi-Fi network.
     *
     * This function performs a permission check for `ACCESS_FINE_LOCATION`, which is required
     * on modern Android versions to access Wi-Fi metadata like the SSID. It uses the
     * `WifiManager.connectionInfo` API to maintain backward compatibility for retrieving
     * active connection details.
     *
     * @param context The application context used to check permissions and access system services.
     * @return A [WifiInfo] object containing details of the current connection if the device
     *         is associated with an access point; `null` if permissions are missing, an error
     *         occurs, or the device is not connected to Wi-Fi.
     */
    fun getWifi(context: Context): WifiInfo? {
        //Centralized permission verification
        val hasLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasLocation) {
            PreyLogger.d("Location permission denied for getWifi")
            return null
        }
        //Using applicationContext to avoid memory leaks
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        //Information gathering (Depreciation management)
        return try {
            //Although connectionInfo is deprecated, it remains the most direct way
            //to obtain the WifiInfo object for backward compatibility.
            wifiMgr?.connectionInfo?.takeIf { it.networkId != -1 }
        } catch (e: Exception) {
            PreyLogger.e("Error obtaining WifiInfo: ${e.message}", e)
            null
        }
    }


    /**
     * Retrieves the human-readable network type (e.g., "WiFi", "2G", "3G", "4G", "5G").
     *
     * This function serves as a router to determine the specific generation of the network
     * connection. It selects between modern and legacy implementation methods based on the
     * Android SDK version and the availability of the `READ_PHONE_STATE` permission.
     *
     * @param context The application context used to check permissions and access connectivity services.
     * @return A string representing the network type, or `null` if the device is disconnected
     *          atau the type cannot be determined.
     */
    fun getNetworkType(context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            getNetworkTypeVersionN(context)
        } else {
            return getNetworkTypeLegacy(context)
        }
    }

    /**
     * Determines the specific network generation (e.g., "2G", "3G", "4G", "5G") or connection type.
     *
     * This function uses modern [ConnectivityManager] and [NetworkCapabilities] APIs available
     * from Android N onwards. It first checks for a Wi-Fi connection and, if not found,
     * evaluates the cellular network type using [TelephonyManager.getDataNetworkType].
     *
     * @param context The application context used to access system services.
     * @return A string representing the connection type ("WiFi", "2G", "3G", "4G", "5G")
     *         or `null` if the connection is unknown, inactive, or not supported.
     * @throws SecurityException If the [Manifest.permission.READ_PHONE_STATE] permission is not granted.
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @RequiresApi(Build.VERSION_CODES.N)
    fun getNetworkTypeVersionN(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        //We check if it's Wi-Fi first
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "WiFi"
        //If it is cellular, we determine the generation
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            //Note: networkType requires READ_PHONE_STATE permission
            return when (telephonyManager.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> null
            }
        }
        return null
    }


    /**
     * Retrieves the network connection type string using legacy APIs.
     *
     * This function uses the deprecated [ConnectivityManager.getActiveNetworkInfo] and
     * [TelephonyManager] network subtypes to determine the connection generation (2G, 3G, 4G, 5G)
     * or if the device is connected via WiFi. It is intended for use on older Android versions
     * or as a fallback when specific permissions for newer APIs are not available.
     *
     * @param context The application context used to access connectivity and telephony services.
     * @return A string representing the connection type ("WiFi", "2G", "3G", "4G", "5G")
     *         or `null` if the device is disconnected or the type is unknown.
     */
    fun getNetworkTypeLegacy(context: Context): String? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        if (info == null || !info.isConnected) return null
        if (info.type == ConnectivityManager.TYPE_WIFI) return "WiFi"
        if (info.type == ConnectivityManager.TYPE_MOBILE) {
            return when (info.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else ->null
            }
        }
        return null
    }

    const val NO_CONNECTION="NO_CONNECTION"
    const val CONNECTION_WIFI="CONNECTION_WIFI"
    const val CONNECTION_MOBILE="CONNECTION_MOBILE"
    const val CONNECTION_ETHERNET="CONNECTION_ETHERNET"

    /**
     * Determines the current network connection type (WiFi, Mobile, or Ethernet).
     *
     * This function checks for the active network transport using version-specific APIs.
     * If the device is running Marshmallow (API 23) or higher and the `READ_PHONE_STATE`
     * permission is not granted, it utilizes [checkNetworkM]. Otherwise, it falls back
     * to [getNetworkLegacy].
     *
     * @param context The application context used to access system services and check permissions.
     * @return A string constant representing the connection type (e.g., [CONNECTION_WIFI],
     *         [CONNECTION_MOBILE], [CONNECTION_ETHERNET]) or [NO_CONNECTION] if no active
     *         network is found.
     */
    fun checkNetwork(context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkNetworkM(context)
        } else {
            getNetworkLegacy(context)
        }
    }

    /**
     * Determines the current network connection type for devices running Android M (API 23) and above.
     *
     * This function utilizes [ConnectivityManager] and [NetworkCapabilities] to identify if the
     * active network is Wi-Fi, Cellular, or Ethernet. It is designed as a modern alternative
     * to the deprecated [ConnectivityManager.activeNetworkInfo] API.
     *
     * @param context The application context used to access the connectivity system service.
     * @return A string constant representing the connection type: [CONNECTION_WIFI],
     *         [CONNECTION_MOBILE], or [CONNECTION_ETHERNET]. Returns [NO_CONNECTION] if
     *         no network is active or the transport type is unrecognized.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkNetworkM(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return NO_CONNECTION
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NO_CONNECTION
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> CONNECTION_WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> CONNECTION_MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> CONNECTION_ETHERNET
            else -> NO_CONNECTION
        }
    }
    /**
     * Determines the current network connection type using a legacy-compatible approach.
     *
     * This function iterates through all available networks and checks their capabilities
     * to identify if a Wi-Fi or Cellular connection is active. It is typically used as a
     * fallback for older Android versions or when specific permission constraints apply.
     *
     * @param context The application context used to access the ConnectivityManager.
     * @return A string constant representing the connection type: [CONNECTION_WIFI],
     *         [CONNECTION_MOBILE], or [NO_CONNECTION] if no matching transport is found.
     */
    fun getNetworkLegacy(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = cm.allNetworks
        for (network in networks) {
            val capabilities = cm.getNetworkCapabilities(network)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return CONNECTION_WIFI
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return CONNECTION_MOBILE
                }
            }
        }
        return NO_CONNECTION
    }

    /**
     * Checks if the device is currently connected to the internet via a mobile (cellular) network.
     *
     * This function uses the [ConnectivityManager] to verify if there is an active network
     * connection and whether that connection is provided by a cellular transport. It handles
     * compatibility across different Android versions, using [NetworkCapabilities] for both
     * modern (API 23+) and legacy (API 21/22) devices.
     *
     * @param context The application context used to access system connectivity services.
     * @return `true` if a mobile data connection is active and available; `false` otherwise.
     */
    fun isMobileDataConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val networks = connectivityManager.allNetworks
            networks.any { network ->
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            }
        }
    }

}