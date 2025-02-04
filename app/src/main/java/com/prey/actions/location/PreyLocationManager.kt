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

class PreyLocationManager {

    var location: PreyLocation? = null

    fun getLastLocation(): PreyLocation? {
        return location
    }

    fun setLastLocation(preyLocation: PreyLocation?) {
        location = preyLocation
    }

    fun isGpsLocationServiceActive(ctx: Context): Boolean {
        val androidLocationManager =
            ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        try {
            gps_enabled =
                androidLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return gps_enabled
    }

    fun isNetworkLocationServiceActive(ctx: Context): Boolean {
        val androidLocationManager =
            ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var network_enabled = false
        try {
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