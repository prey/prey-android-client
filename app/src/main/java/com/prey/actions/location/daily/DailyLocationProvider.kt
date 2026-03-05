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
import kotlin.coroutines.resume

/**
 * Provides access to the device's location using the Google Play Services Fused Location Provider.
 *
 * This class abstracts the complexities of interacting with the [FusedLocationProviderClient]
 * and provides a coroutine-friendly way to retrieve the current location.
 *
 * @property context The application context used to initialize the location services.
 */
class DailyLocationProvider(context: Context) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Retrieves the current location of the device.
     *
     * This function uses the Fused Location Provider to request a single location update
     * with high accuracy. It will attempt to return a location that is at most 60 seconds old
     * or request a fresh one if necessary.
     *
     * @return The current [Location] if successfully retrieved, or `null` if the location
     *         could not be determined or an error occurred.
     * @throws SecurityException If the required location permissions are not granted.
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(60_000)
                .build()
            fusedClient.getCurrentLocation(request, null)
                .addOnSuccessListener { location ->
                    cont.resume(location)
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
}