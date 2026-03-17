/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Utility object responsible for retrieving high-precision location data.
 *
 * This provider leverages the Fused Location Provider API to obtain the most accurate
 * current location, applying specific constraints on accuracy and timeout to ensure
 * data quality for daily tracking routines.
 */
object DailyLocationProvider {

    /**
     * Fetches the current device location with high precision using the Fused Location Provider.
     *
     * This function attempts to retrieve a location with [Priority.PRIORITY_HIGH_ACCURACY].
     * It includes a 30-second timeout and enforces an accuracy threshold of 75 meters.
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    suspend fun fetchPreciseLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return withTimeoutOrNull(30_000) { //30-second time limit
            suspendCancellableCoroutine { continuation ->
                try {
                    //High-precision configuration
                    val currentLocationRequest = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setMaxUpdateAgeMillis(60_000) //Do not use locations older than 60 seconds
                        .build()
                    fusedLocationClient.getCurrentLocation(currentLocationRequest, null)
                        .addOnSuccessListener { location: Location? ->
                            if (location != null && location.accuracy <= 75f) {
                                continuation.resume(location)
                            } else {
                                //Insufficient accuracy or null, will retry in 30 minutes
                                continuation.resume(null)
                            }
                        }
                        .addOnFailureListener {
                            continuation.resume(null)
                        }
                } catch (e: SecurityException) {
                    continuation.resume(null)
                }
            }
        }
    }

}