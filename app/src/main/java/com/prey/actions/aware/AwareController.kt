/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.prey.FileConfigReader
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.location.LastLocationService
import com.prey.actions.location.LocationUtil
import com.prey.actions.location.PreyLocation
import com.prey.actions.location.UpdateLocationService
import com.prey.actions.location.daily.DailyLocationService
import com.prey.events.factories.EventFactory
import com.prey.net.PreyHttpResponse
import com.prey.receivers.AwareGeofenceReceiver
import org.json.JSONObject
import java.net.HttpURLConnection
import java.util.Date

/**
 * Controller class for handling location awareness.
 */
class AwareController {

    /**
     * Starts a location service.
     *
     * @param context Context
     * @param serviceClass Class of the service to start
     */
    private fun startLocationService(context: Context, serviceClass: Class<*>) {
        try {
            val serviceIntent = Intent(context, serviceClass)
            context.startService(serviceIntent)
            Thread.sleep(3000)
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            PreyLogger.d("Error:${e.message}")
        }
    }

    /**
     * Initializes the last location.
     *
     * @param context Context
     */
    fun initLastLocation(context: Context) {
        startLocationService(context, LastLocationService::class.java)
    }

    /**
     * Initializes the update location.
     *
     * @param context Context
     */
    @SuppressLint("SuspiciousIndentation")
    fun initUpdateLocation(context: Context) {
        startLocationService(context, UpdateLocationService::class.java)
    }

    /**
     * Initializes the daily location.
     *
     * @param context Context
     */
    @SuppressLint("SuspiciousIndentation")
    fun initDailyLocation(context: Context) {
        startLocationService(context, DailyLocationService::class.java)
    }

    /**
     * Creates a pending intent for geofencing.
     *
     * @param context Context
     * @return PendingIntent
     */
    private fun createGeofencingPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_GEOFENCE,
            Intent(context, AwareGeofenceReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Creates a geofencing request.
     *
     * @param context Context
     * @return GeofencingRequest?
     */
    private fun createGeofencingRequest(context: Context): GeofencingRequest? {
        try {
            val geofenceConfig = FileConfigReader.getInstance(context) ?: return null
            val awareRadius = geofenceConfig.getRadiusAware()
            val lastLocation = PreyConfig.getInstance(context).getLocationAware() ?: return null
            val geofence = Geofence.Builder()
                .setRequestId(GEO_AWARE_NAME)
                .setCircularRegion(
                    lastLocation.getLat(),
                    lastLocation.getLng(),
                    awareRadius.toFloat()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
            return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT or GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error createGeofencingRequest:${e.message}", e)
            return null
        }
    }

    /**
     * Registers a geofence.
     *
     * @param context Context
     */
    @SuppressLint("MissingPermission")
    fun registerGeofence(context: Context) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val createGeofencingRequest = createGeofencingRequest(context) ?: return
        geofencingClient.addGeofences(
            createGeofencingRequest,
            createGeofencingPendingIntent(context)
        )
            .run {
                addOnSuccessListener {
                    PreyLogger.d("AWARE registerGeofence: SUCCESS")
                }
                addOnFailureListener { exception ->
                    PreyLogger.d("AWARE registerGeofence: Failure\n$exception")
                }
            }
    }

    /**
     * Sends an aware notification.
     *
     * @param context Context
     * @param currentLocation Current location
     * @return PreyLocation?
     * @throws Exception
     */
    @Throws(Exception::class)
    fun sendAware(context: Context, currentLocation: PreyLocation): PreyLocation? {
        //get location
        val previousLocation = PreyConfig.getInstance(context).getLocationAware()
        val distanceThreshold = PreyConfig.getInstance(context).getDistanceAware()
        //TODO:SACAR DISTANCIA
        val shouldSendNotification =
            true//mustSendAware(context, previousLocation, currentLocation, distanceAware)
        //send aware
        return if (shouldSendNotification) {
            sendNowAware(context, currentLocation)
            currentLocation
        } else {
            null
        }
    }

    /**
     * Sends a daily location update.
     *
     * @param context Context
     * @param location PreyLocation
     * @throws Exception
     */
    @Throws(Exception::class)
    fun sendDaily(context: Context, location: PreyLocation) {
        val formattedAccuracy = Math.round(location.getAccuracy() * 100.0) / 100.0
        val json = JSONObject().apply {
            put("lat", location.getLat())
            put("lng", location.getLng())
            put("accuracy", formattedAccuracy)
            put("method", location.getMethod() ?: "native")
            put("force", true)
        }
        val locationJson = JSONObject()
        locationJson.put("location", json)
        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        val response = PreyConfig.getInstance(context).getWebServices().sendLocation(context, locationJson)
        if (response != null) {
            val statusCode = response.getStatusCode()
            PreyLogger.d("DAILY getStatusCode :${statusCode}")
            if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED) {
                PreyConfig.getInstance(context).setDailyLocation(
                    PreyConfig.FORMAT_SDF_AWARE.format(
                        Date()
                    )
                )
            }
            PreyLogger.d("DAILY sendNowAware:${location.toString()}")
        }
    }

    /**
     * Method to if location must send
     * @param context Context
     * @param oldLocation Old location
     * @param newLocation New location
     * @param distanceAware Minimum difference between locations
     * @return returns if to send
     */
    fun mustSendAware(
        context: Context?,
        previousLocation: PreyLocation?,
        currentLocation: PreyLocation?,
        distanceAware: Int
    ): Boolean {
        if (previousLocation == null && currentLocation != null) {
            return true
        }
        if (previousLocation != null && currentLocation != null) {
            val distanceBetweenLocations = LocationUtil.distance(previousLocation, currentLocation)
            return distanceBetweenLocations > distanceAware
        }
        return false
    }

    /**
     * Method that sends the location
     * @param context Context
     * @param currentLocation  location
     * @return returns PreyHttpResponse
     */
    @Throws(Exception::class)
    fun sendNowAware(context: Context, currentLocation: PreyLocation?): PreyHttpResponse? {
        if (currentLocation == null || currentLocation.getLat() == 0.0 || currentLocation.getLng() == 0.0) {
            return null
        }
        val isLocationAwareEnabled =
            PreyConfig.getInstance(context).isLocatigetAwareonAwareEnabled()
        if (!isLocationAwareEnabled) {
            return null
        }
        val dailyLocation = PreyConfig.getInstance(context).getDailyLocation()
        val currentDailyLocation = PreyConfig.FORMAT_SDF_AWARE.format(Date())
        val isAirplaneModeEnabled: Boolean = EventFactory.isAirplaneModeOn(context)
        PreyLogger.i("AWARE dailyLocation:$dailyLocation currentDailyLocation:$currentDailyLocation isAirplaneModeEnabled:$isAirplaneModeEnabled")
        val shouldForceUpdate = (currentDailyLocation != dailyLocation && !isAirplaneModeEnabled)
        PreyLogger.i("AWARE shouldForceUpdate:$shouldForceUpdate")
        val locationData = createLocationData(context, currentLocation, shouldForceUpdate)
        val locationWrapper = JSONObject().apply { put("location", locationData) }
        if (Build.VERSION.SDK_INT > 9) {
            val threadPolicy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(threadPolicy)
        }
        val response = PreyConfig.getInstance(context).getWebServices().sendLocation(context, locationWrapper)
        return if (response != null && (response.getStatusCode() == HttpURLConnection.HTTP_CREATED || response.getStatusCode() == HttpURLConnection.HTTP_OK)) {
            // Set location aware data in config
            PreyConfig.getInstance(context).setLocationAware(currentLocation)
            PreyConfig.getInstance(context).setAwareTime()
            val responseAsString = response.getResponseAsString()
            // The date of the last location sent correctly is saved (yyyy-MM-dd )
            val formatAware= PreyConfig.FORMAT_SDF_AWARE.format(Date())
            if(shouldForceUpdate) {
                PreyConfig.getInstance(context).setDailyLocation(formatAware)
            }
            PreyLogger.i("AWARE responseAsString:$responseAsString")
            // Check if response is "OK"
            if ("OK" == responseAsString) {
                PreyLogger.i("AWARE formatAware:$formatAware")
                PreyConfig.getInstance(context).setAwareDate(formatAware)
                // Log location aware data
                PreyLogger.d("AWARE sendNowAware:${currentLocation.toString()}")
            }
            response
        } else {
            null
        }
    }

    /**
     * Creates a JSONObject containing location data from a PreyLocation object.
     *
     * @param location The PreyLocation object to extract data from.
     * @return A JSONObject containing the location data.
     */
    private fun createLocationData(context:Context, location: PreyLocation, shouldForceUpdate: Boolean): JSONObject {
        val locationData = JSONObject()
        locationData.apply {
            put("lat", location.getLat())
            put("lng", location.getLng())
            put("accuracy", roundAccuracy(location.getAccuracy().toDouble()))
            put("method", location.getMethod() ?: "native")
            if(shouldForceUpdate) put("force",true)
        }
        return locationData
    }

    /**
     * Rounds a given accuracy value to two decimal places.
     *
     * @param accuracy The accuracy value to round.
     * @return The rounded accuracy value.
     */
    private fun roundAccuracy(accuracy: Double): Double {
        return Math.round(accuracy * 100.0) / 100.0
    }

    companion object {
        const val GEO_AWARE_NAME = "AWARE"
        const val CUSTOM_REQUEST_CODE_GEOFENCE = 1001

        private var instance: AwareController? = null
        fun getInstance(): AwareController {
            return instance ?: AwareController().also { instance = it }
        }
    }

}