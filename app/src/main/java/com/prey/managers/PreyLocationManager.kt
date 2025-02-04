/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers

import android.content.Context
import android.location.LocationManager
import com.prey.actions.location.PreyLocation
import com.prey.PreyLogger

class PreyLocationManager {
    private var lastLocation: PreyLocation? = null

    fun getLastLocation(): PreyLocation? {
        return lastLocation
    }

    fun setLastLocation(preyLocation: PreyLocation) {
        lastLocation = preyLocation
    }

    fun isGpsLocationServiceActive(ctx: Context): Boolean {

        var gps_enabled = false
        try {
            val androidLocationManager =
                ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            gps_enabled =
                androidLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return gps_enabled
    }

    fun isNetworkLocationServiceActive(ctx: Context): Boolean {

        var network_enabled = false
        try {
            val androidLocationManager =
                ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            network_enabled =
                androidLocationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return network_enabled
    }

    fun locationServicesEnabled(ctx: Context): Boolean {
        return (isGpsLocationServiceActive(ctx) || isNetworkLocationServiceActive(ctx))
    }

    companion object {
        private var INSTANCE: PreyLocationManager? = null
        fun getInstance(): PreyLocationManager {
            if (INSTANCE == null) {
                INSTANCE = PreyLocationManager()
            }
            return INSTANCE!!
        }
    }
}