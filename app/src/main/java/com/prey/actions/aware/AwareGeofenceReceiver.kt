/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import com.prey.PreyLogger
import com.prey.actions.location.LocationSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A [BroadcastReceiver] that listens for geofence transition events.
 *
 * This receiver is triggered when the device enters or exits a geofence previously registered
 * by the application. Upon receiving a geofence event, it performs the following actions:
 * 1. Retrieves the current device location using the Fused Location Provider.
 * 2. Calculates a new, dynamic radius for the next geofence based on the current and previous
 *    locations to adapt to the user's movement speed.
 * 3. Saves the current location and timestamp for future radius calculations.
 * 4. Creates and registers a new geofence centered at the current location with the newly
 *    calculated radius.
 *
 * This mechanism creates a "moving" or "dynamic" geofence that follows the device, allowing for
 * efficient location tracking by only triggering updates when the device moves a significant
 * distance.
 *
 * @see com.google.android.gms.location.GeofencingEvent
 * @see AwareGeofenceManager
 */
class AwareGeofenceReceiver : BroadcastReceiver() {

    /**
     * Receives geofencing events triggered by the system.
     *
     * This method is called when the device exits a previously defined geofence. It handles the
     * event by performing the following steps:
     * 1. Parses the [GeofencingEvent] from the incoming [Intent].
     * 2. Checks for necessary permissions (ACCESS_FINE_LOCATION).
     * 3. Fetches the device's last known location.
     * 4. Calculates a new dynamic geofence radius based on the current and previous locations.
     * 5. Saves the current location and time for future calculations.
     * 6. Creates and registers a new geofence with the updated location and radius.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent containing the geofence event details.
     */
    override fun onReceive(context: Context, intent: Intent) {
        PreyLogger.i("AwareGeofenceReceiver onReceive");
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_EXIT) return
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return
        val fused = LocationServices.getFusedLocationProviderClient(context)
        fused.lastLocation.addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener
            PreyLogger.i("onReceive lastLocation location lat:${location.latitude} lng:${location.longitude} acc:${location.accuracy}")
            val previous = AwareStore.load(context)
            val now = System.currentTimeMillis()
            val radius = if (previous != null) {
                DynamicRadiusCalculator.calculateRadius(
                    previous.location,
                    previous.time,
                    location,
                    now
                )
            } else {
                500f
            }
            CoroutineScope(Dispatchers.IO).launch {
                LocationSender.sendLocationAware(context, location);
            }
            AwareGeofenceManagerImpl(context)
                .createOrReplace(location, radius)
        }
    }

}