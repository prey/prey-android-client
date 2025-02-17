/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location

import android.content.Context
import android.location.LocationManager

import com.prey.PreyLogger

/**
 * Manager class for handling location-related operations.
 */
class PreyLocationManager {

    /**
     * The last known location.
     */
    var location: PreyLocation? = null

    fun getLastLocation(): PreyLocation? {
        return location
    }

    fun setLastLocation(preyLocation: PreyLocation?) {
        location = preyLocation
    }

    /**
     * Check if the GPS location service is active.
     *
     * @param context The application context.
     * @return True if the GPS location service is active, false otherwise.
     */
    fun isGpsLocationServiceActive(context: Context): Boolean {
        val androidLocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        try {
            gps_enabled =
                androidLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return gps_enabled
    }

    /**
     * Check if the network location service is active.
     *
     * @param context The application context.
     * @return True if the network location service is active, false otherwise.
     */
    fun isNetworkLocationServiceActive(context: Context): Boolean {
        val androidLocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var network_enabled = false
        try {
            network_enabled =
                androidLocationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return network_enabled
    }

    /**
     * Check if any location service is active.
     *
     * @param context The application context.
     * @return True if any location service is active, false otherwise.
     */
    fun locationServicesEnabled(context: Context): Boolean {
        return (isGpsLocationServiceActive(context) || isNetworkLocationServiceActive(context))
    }

    companion object {
        private var instance: PreyLocationManager? = null
        fun getInstance(): PreyLocationManager {
            return instance ?: PreyLocationManager().also { instance = it }
        }
    }
}