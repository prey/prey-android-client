/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.prey.PreyLogger
import com.prey.actions.location.LocationSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Provides the initial device location using the Fused Location Provider from Google Play Services.
 * This class is responsible for fetching the current or last known location to set up
 * initial location-based awareness features, such as creating a geofence around the user's
 * starting point.
 *
 * @param context The application context, used to access location services.
 */
class AwareInitialLocationProvider(private val context: Context) {

    private val fused = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Asynchronously retrieves the initial device location.
     *
     * This function first attempts to get the current, fresh location. If that fails (e.g., location is disabled
     * or the request times out), it falls back to fetching the last known location from the Fused Location Provider.
     * The resulting location is then passed to the `onSuccess` callback.
     *
     * Note: This function suppresses the `MissingPermission` lint warning. The caller is responsible for
     * ensuring that the necessary location permissions (`ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`)
     * have been granted before invoking this method.
     *
     * @param onSuccess A lambda function that will be executed with the retrieved [Location] object upon success.
     *                  This callback is not guaranteed to be invoked if a location cannot be determined.
     */
    @SuppressLint("MissingPermission")
    fun getInitialLocation(onSuccess: (Location) -> Unit) {
        fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) onSuccess(location)
                else fused.lastLocation.addOnSuccessListener { it?.let(onSuccess) }
            }
    }

    /**
     * Initializes the location-aware features by fetching the device's current location.
     *
     * This function performs the following steps:
     * 1. Obtains the current or last known location.
     * 2. Saves this location to [AwareStore].
     * 3. Creates a geofence around this initial location with a radius of 300 meters.
     * 4. Sends the location immediately as an "aware" report.
     *
     * This process is crucial for setting up the initial state for geofence-based tracking.
     *
     * Requires [android.Manifest.permission.ACCESS_FINE_LOCATION] permission.
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    fun init() {
        PreyLogger.i("AwareInitialLocationProvider")
        AwareInitialLocationProvider(context)
            .getInitialLocation { location ->
                PreyLogger.i("init location lat:${location.latitude} lng:${location.longitude} acc:${location.accuracy}")
                AwareGeofenceManagerImpl(context)
                    .createOrReplace(location, radius = 300f)
                PreyLogger.i("sendNowAware");
                CoroutineScope(Dispatchers.IO).launch {
                    LocationSender.sendLocationAware(context, location);
                }
            }
    }

}