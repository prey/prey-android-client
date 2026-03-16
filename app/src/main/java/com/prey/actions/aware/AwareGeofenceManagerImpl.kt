/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.prey.PreyLogger

/**
 * Manages the creation and replacement of a single, app-wide geofence.
 *
 * This implementation uses the Google Play Services [GeofencingClient] to monitor a circular
 * geographic area. It is designed to handle a single, global geofence identified by [GEOFENCE_ID].
 * When a new geofence is created, any existing one with the same ID is removed and then replaced.
 * The geofence triggers an event only on exiting the defined area.
 *
 * @property context The application context used to access system services like [GeofencingClient].
 * @property client The [GeofencingClient] instance used to add and remove geofences.
 */
class AwareGeofenceManagerImpl(
    private val context: Context,
    private val client: GeofencingClient =
        LocationServices.getGeofencingClient(context)
) {

    /**
     * Creates a new geofence or replaces an existing one with the same ID.
     *
     * This function first removes any existing geofence with the ID [GEOFENCE_ID] to ensure a clean state.
     * It then constructs and adds a new circular geofence centered at the specified [location] with the given [radius].
     * The geofence is configured to trigger only on exit events ([Geofence.GEOFENCE_TRANSITION_EXIT]) and never expires.
     *
     * A [PendingIntent] is used to notify [AwareGeofenceReceiver] when the geofence transition occurs.
     *
     * Requires [android.Manifest.permission.ACCESS_FINE_LOCATION] permission. The function will return early
     * if this permission is not granted.
     *
     * @param location The center point of the geofence.
     * @param radius The radius of the geofence in meters.
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    fun createOrReplace(location: Location, radius: Float) {
        if (!hasPermission()) return
        PreyLogger.i("createOrReplace lat:${location.latitude} long:${location.longitude} radius:$radius")
        client.removeGeofences(listOf(GEOFENCE_ID))
            .addOnCompleteListener {
                PreyLogger.i("addOnCompleteListener")
                val geofence = Geofence.Builder()
                    .setRequestId(GEOFENCE_ID)
                    .setCircularRegion(
                        location.latitude,
                        location.longitude,
                        radius
                    )
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_EXIT
                    )
                    .setExpirationDuration(
                        Geofence.NEVER_EXPIRE
                    )
                    .setNotificationResponsiveness(60000)
                    .build()
                client.addGeofences(
                    GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
                        .addGeofence(geofence)
                        .build(),
                    pendingIntent
                )
                PreyLogger.i("addGeofences")
            }
    }

    private fun hasPermission() =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * A [PendingIntent] that triggers [AwareGeofenceReceiver] when a geofence transition occurs.
     *
     * This intent is lazily initialized and configured to cancel any existing intents of the same type
     * while remaining mutable to allow the system to provide transition details.
     */
    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AwareGeofenceReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    companion object {
        const val GEOFENCE_ID = "GLOBAL_DYNAMIC_GEOFENCE"
    }
}
