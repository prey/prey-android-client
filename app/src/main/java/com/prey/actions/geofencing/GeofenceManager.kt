package com.prey.actions.geofencing

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.prey.FileConfigReader
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.aware.AwareController
import com.prey.actions.location.PreyLocation
import com.prey.actions.location.PreyLocationManager
import kotlinx.coroutines.tasks.await

class GeofenceManager(val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    // Initialize the FusedLocationProviderClient instance.
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    val geofenceList = mutableMapOf<String, Geofence>()

    private val geofencingPendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_GEOFENCE,
            Intent(context, GeofenceBroadcastReceiver::class.java),
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE
            }
        )
    }

    fun initGeofence() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            PreyLogger.d("AWARE GeofenceManager initGeofence not permission")
            return
        } else {
            locationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    PreyLogger.d("AWARE GeofenceManager New location: $location")
                    val preyLocation = PreyLocation(location)
                    PreyLocationManager.getInstance().setLastLocation(preyLocation)
                    PreyConfig.getInstance(context).setLocation(preyLocation)
                    PreyConfig.getInstance(context).setLocationAware(preyLocation)

                    addGeofence(GEOFENCE_ID, location)
                    registerGeofence()
                    AwareController.getInstance().sendAware(context, preyLocation)
                }
            }
        }
    }

    fun updateGeofence(location: Location) {
        PreyLogger.d("AWARE GeofenceManager New location: $location")
        val preyLocation = PreyLocation(location)
        PreyLocationManager.getInstance().setLastLocation(preyLocation)
        PreyConfig.getInstance(context).setLocation(preyLocation)
        PreyConfig.getInstance(context).setLocationAware(preyLocation)
        addGeofence(GEOFENCE_ID, location)
        registerGeofence()
        AwareController.getInstance().sendAware(context, preyLocation)
    }

    fun addGeofence(
        key: String,
        location: Location
    ) {
        geofenceList[key] = createGeofence(key, location)
    }

    fun removeGeofence(key: String) {
        geofenceList.remove(key)
    }

    @SuppressLint("MissingPermission")
    fun registerGeofence() {
        geofencingClient.addGeofences(createGeofencingRequest(), geofencingPendingIntent)
            .addOnSuccessListener {
                PreyLogger.d("AWARE GeofenceManager registerGeofence: SUCCESS")
            }.addOnFailureListener { exception ->
                PreyLogger.d("AWARE GeofenceManager registerGeofence: Failure\n$exception")
            }
    }

    suspend fun deregisterGeofence() = kotlin.runCatching {
        geofencingClient.removeGeofences(geofencingPendingIntent).await()
        geofenceList.clear()
    }

    private fun createGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT or GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList.values.toList())
        }.build()
    }

    private fun createGeofence(
        key: String,
        location: Location
    ): Geofence {
        val geofenceConfig = FileConfigReader.getInstance(context)
        val radiusInMeters = geofenceConfig.getRadiusAware()
        PreyLogger.d("AWARE GeofenceManager createGeofence radiusInMeters:$radiusInMeters")
        return Geofence.Builder()
            .setRequestId(key)
            .setCircularRegion(location.latitude, location.longitude, radiusInMeters.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    companion object {
        private var instance: GeofenceManager? = null
        fun getInstance(context: Context): GeofenceManager {
            return instance ?: GeofenceManager(context).also { instance = it }
        }

        val GEOFENCE_ID = "GEOFENCE_ID"
        val CUSTOM_REQUEST_CODE_GEOFENCE = 10001
    }

}