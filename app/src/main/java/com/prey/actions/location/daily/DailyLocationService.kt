/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.aware.AwareController
import com.prey.actions.location.LocationUtil
import com.prey.actions.location.PreyLocation

/**
 * A service that provides daily location updates.
 */
class DailyLocationService : Service() {
    private var mLocationRequest: LocationRequest? = null

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var mLocation: Location? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Called when the service is created.
     *
     * This method is called only once, when the service is first created.
     * It is used to initialize the service and set up any necessary resources.
     */
    override fun onCreate() {
        super.onCreate()
        startForegroundService(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    /**
     * Starts the foreground service.
     *
     * @param context The context of the service.
     */
    private fun startForegroundService(context: Context) {
        PreyLogger.d("AWARE DailyLocationService Start foreground service.kt")
        try {
            mLocationRequest = LocationRequest.create()
            mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
            mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest!!,
                mLocationCallback,
                Looper.myLooper()!!
            )
        } catch (unlikely: SecurityException) {
            PreyLogger.e("DailyLocationService error:${unlikely.message}", unlikely)
        }
    }

    /**
     * Location callback object that receives location updates.
     */
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            try {
                val mLocation = locationResult.lastLocation!!
                val latitude = mLocation.latitude
                val longitude = mLocation.longitude
                val accuracy = mLocation.accuracy
                val context = applicationContext
                val preyLocation = PreyLocation(mLocation)
                PreyConfig.getInstance(context).setLocation(preyLocation)
                PreyConfig.getInstance(context).setLocationAware(preyLocation)
                PreyLogger.d(
                    "AWARE mLocationCallback Location lat:${LocationUtil.round(latitude)} long:${
                        LocationUtil.round(
                            longitude
                        )
                    } acc:${
                        LocationUtil.round(
                            accuracy.toDouble()
                        )
                    }"
                )
                AwareController.getInstance().sendDaily(context, preyLocation)
                stop()
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
        }
    }

    /**
     * Stops the service.
     */
    private fun stop() {
        PreyLogger.d("AWARE DailyLocationService stop")
        try {
            stopSelf()
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    companion object {
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

}