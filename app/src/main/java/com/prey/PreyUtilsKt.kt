package com.prey

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility object containing extension functions and helper methods for the Prey application.
 */
object PreyUtilsKt {

    /**
     * Determines if the current device is a tablet based on its smallest screen width.
     *
     * A device is considered a tablet if its smallest screen width (sw) is at least 600dp.
     *
     * @return true if the device is a tablet, false otherwise.
     */
    fun Context.isTablet(): Boolean {
        return resources.configuration.smallestScreenWidthDp >= 600
    }

    /**
     * Checks whether the device has an active and validated internet connection.
     *
     * This function uses [NetworkCapabilities] for devices running Android M (API 23) and above,
     * and falls back to the legacy [ConnectivityManager.activeNetworkInfo] for older versions.
     *
     * @param context The [Context] used to access the system connectivity service.
     * @return `true` if the network is available and has internet capability, `false` otherwise.
     */
    fun isNetworkAvailable(context:Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //We obtain the current active network
            val network = connectivityManager.activeNetwork ?: return false
            //We obtain the capabilities of that network
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            //Deprecated in API 29+, but required for API 21/22
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }
    }

}