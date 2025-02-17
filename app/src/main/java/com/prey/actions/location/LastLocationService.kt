/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.aware.AwareController

/**
 * Service responsible for getting the last known location of the device.
 */
class LastLocationService : Service() {
    // Variables to store location request and callback
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null

    // Variable to store the last known location
    private var mLocation: Location? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

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
     * @param context Context of the application
     */
    fun startForegroundService(context: Context) {
        PreyLogger.d("AWARE LocationUpdatesService Start foreground service.kt")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mLocation = locationResult.lastLocation
                PreyLogger.d("AWARE LocationUpdatesService New location_: $mLocation")
                PreyLocationManager.getInstance().setLastLocation(PreyLocation(mLocation))
            }
        }
        try {
            createLocationRequest()
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        getLastLocation(context)
    }

    /**
     * Creates a location request.
     */
    private fun createLocationRequest() {
        PreyLogger.d("LocationUpdatesService createLocationRequest: ")
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    /**
     * Gets the last known location.
     *
     * @param context Context of the application
     */
    private fun getLastLocation(context: Context) {
        PreyLogger.d("LocationUpdatesService getLastLocation")
        try {
            mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                PreyLogger.d("LocationUpdatesService onComplete kt")
                if (task.isSuccessful && task.result != null) {
                    mLocation = task.result
                    PreyLogger.d(
                        "AWARE LastLocationService lat__ :" + LocationUtil.round(
                            mLocation!!.latitude
                        ) + " lng:" + LocationUtil.round(
                            mLocation!!.longitude
                        ) + " acc:" + LocationUtil.round(
                            mLocation!!.accuracy.toDouble()
                        )
                    )
                    val preyLocation = PreyLocation(mLocation)

                    PreyConfig.getInstance(context).setLocation(preyLocation)
                    PreyConfig.getInstance(context).setLocationAware(preyLocation)
                    AwareController.getInstance().sendAware(context, preyLocation)
                    stop()
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    /**
     * Stops the service.
     */
    private fun stop() {
        PreyLogger.d("LocationUpdatesService stop")
        try {
            if (mLocationCallback != null) mFusedLocationClient!!.removeLocationUpdates(
                mLocationCallback!!
            )
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
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