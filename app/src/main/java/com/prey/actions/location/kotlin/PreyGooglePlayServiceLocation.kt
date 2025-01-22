/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.kotlin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class PreyGooglePlayServiceLocation : GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    protected var mGoogleApiClient: GoogleApiClient? = null
    protected var mLocationRequest: LocationRequest? = null
    protected var mCurrentLocation: Location? = null
    protected var mLastUpdateTime: String? = null
    protected var mRequestingLocationUpdates: Boolean? = null
    private var ctx: Context? = null

    fun init(ctx: Context?) {
        this.ctx = ctx
        mCurrentLocation = null
        mLastUpdateTime = null
        mRequestingLocationUpdates = false
        buildGoogleApiClient()
    }

    fun getLastLocation(ctx: Context?): Location? {
        return mCurrentLocation
    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(ctx!!)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        val connectionResult = mGoogleApiClient!!.blockingConnect(
            GOOGLE_API_CLIENT_TIMEOUT_S.toLong(), TimeUnit.SECONDS
        )
        if (connectionResult.isSuccess && mGoogleApiClient!!.isConnected) {
            createLocationRequest()
            startLocationUpdates()
        } else {
            PreyLogger.d(
                String.format(
                    GOOGLE_API_CLIENT_ERROR_MSG,
                    connectionResult.errorCode
                )
            )
        }
    }

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setInterval(PreyConfig.UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setFastestInterval(PreyConfig.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setPriority(PreyConfig.LOCATION_PRIORITY_HIGHT)
    }

    override fun onConnected(connectionHint: Bundle?) {
    }

    override fun onLocationChanged(location: Location) {
        mCurrentLocation = location
        if (location != null) {
            mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
            stopLocationUpdates()
        }
    }

    fun stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient!!, this)
    }

    override fun onConnectionSuspended(cause: Int) {
        PreyLogger.d("Connection suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        PreyLogger.d("Connection failed: ConnectionResult.getErrorCode() = " + result.errorCode)
    }

    protected fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || (ActivityCompat.checkSelfPermission(
                ctx!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                ctx!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            try {
                Looper.prepare()
                LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient!!, mLocationRequest!!, this
                )
                Looper.loop()
            } catch (e: Exception) {
                PreyLogger.d("Error startLocationUpdates: " + e.message)
            }
        }
    }

    companion object {
        const val GOOGLE_API_CLIENT_TIMEOUT_S: Int = 10
        const val GOOGLE_API_CLIENT_ERROR_MSG: String =
            "Failed to connect to GoogleApiClient (error code = %d)"
    }
}