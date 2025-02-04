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
import java.text.DecimalFormat

class LocationUpdatesService : Service() {
    private var mLocationRequest: LocationRequest? = null

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var mLocationCallback: LocationCallback? = null

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

    fun startForegroundService(ctx: Context) {
        PreyLogger.d("LocationUpdatesService Start foreground service.kt")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mLocation = locationResult.lastLocation
                PreyLogger.d("LocationUpdatesService New location_: $mLocation")
                PreyLocationManager.getInstance().setLastLocation(PreyLocation(mLocation))

            }
        }
        try {
            createLocationRequest()
        } catch (e: Exception) {
            PreyLogger.e("LocationUpdatesService error:" + e.message, e)
        }

        getLastLocation(ctx)
    }

    private fun createLocationRequest() {
        PreyLogger.d("LocationUpdatesService createLocationRequest: ")
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    private fun getLastLocation(ctx: Context) {
        PreyLogger.d("LocationUpdatesService getLastLocation")
        try {
            mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                    PreyLogger.d("LocationUpdatesService onComplete kt")
                    if (task.isSuccessful && task.result != null) {
                        mLocation = task.result
                        PreyLogger.d(
                            "LocationUpdatesService mLocation lat__ :" + round(
                                mLocation!!.latitude
                            ) + " lng:" + round(
                                mLocation!!.longitude
                            ) + " acc:" + round(
                                mLocation!!.accuracy.toDouble()
                            )
                        )
                        PreyLocationManager.getInstance().setLastLocation(
                            PreyLocation(mLocation)
                        )
                        PreyConfig.getInstance(ctx).setLocation(PreyLocation(mLocation))
                    } else {
                        PreyLogger.d("LocationUpdatesService Failed to get location.")
                    }
                }
            stop()
        } catch (unlikely: SecurityException) {
            PreyLogger.e("LocationUpdatesService error:" + unlikely.message, unlikely)
        }
    }

    private fun stop() {
        PreyLogger.d("LocationUpdatesService stop")
        try {
            if (mLocationCallback != null) mFusedLocationClient!!.removeLocationUpdates(
                mLocationCallback!!
            )
        } catch (e: Exception) {
            PreyLogger.e("error." + e.message, e)
        }
        try {
            stopSelf()
        } catch (e: Exception) {
            PreyLogger.e("error." + e.message, e)
        }
    }

    companion object {
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2

        fun round(value: Double): Double {
            var finalValue = 0.0
            val df = DecimalFormat("0.000000")
            val format = df.format(value)
            try {
                finalValue = df.parse(format) as Double
            } catch (e1: Exception) {
                try {
                    val finalValue2 = df.parse(format) as Long
                    finalValue = finalValue2.toDouble()
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            return finalValue
        }
    }
}