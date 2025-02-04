/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

import com.prey.actions.location.LocationUpdatesService
import com.prey.actions.location.LocationUtil
import com.prey.actions.location.PreyLocation
import com.prey.actions.location.PreyLocationManager
import com.prey.FileConfigReader
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyHttpResponse
import com.prey.net.PreyWebServices
import com.prey.receivers.AwareGeofenceReceiver


import org.json.JSONObject
import java.net.HttpURLConnection
import java.util.Date


class AwareController {
    fun init(context: Context) {
        try {
            val isLocationAwareEnabled = PreyConfig.getInstance(context).getAware()
            PreyLogger.d("AWARE AwareController init isLocationAwareEnabled:$isLocationAwareEnabled")
            if (isLocationAwareEnabled) {
                PreyLocationManager.getInstance().setLastLocation(null)
                val locationNow = LocationUtil.getLocation(context, null, false)
                if (locationNow != null && locationNow.isValid()) {
                    PreyLocationManager.getInstance().setLastLocation(locationNow)
                    PreyLogger.d("AWARE locationNow[i]:$locationNow")
                }
                LocationUpdatesService().startForegroundService(context)
                var locationAware: PreyLocation? = null
                var i = 0
                while (i < LocationUtil.MAXIMUM_OF_ATTEMPTS) {
                    PreyLogger.d("AWARE getPreyLocationApp[i]:$i")
                    try {
                        Thread.sleep((LocationUtil.SLEEP_OF_ATTEMPTS[i] * 1000).toLong())
                    } catch (e: InterruptedException) {
                    }
                    locationAware = PreyLocationManager.getInstance().getLastLocation()
                    if (locationAware != null) {
                        locationAware.setMethod("native");
                        PreyLogger.d("AWARE init:$locationAware")
                    }
                    if (locationAware != null && locationAware.isValid()) {
                        break
                    }
                    i++
                }
                val locationNow2 = sendAware(context, locationAware)
                if (locationNow2 != null) {
                    run(context)
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("AWARE error:" + e.message, e)
        }
    }

    fun run(context: Context) {
        PreyLogger.d("AWARE AwareController run")
        try {
            val geofenceConfig = FileConfigReader.getInstance(context)!!
            val loiteringDelay = geofenceConfig.geofenceLoiteringDelay
            val notificationResponsiveness = geofenceConfig.geofenceNotificationResponsiveness
            val radius = geofenceConfig.radiusAware

            //remove
            val listRemove: MutableList<String> = ArrayList()
            listRemove.add(GEO_AWARE_NAME)
            LocationServices.getGeofencingClient(context!!).removeGeofences(listRemove)
            //new
            val locationOld = PreyConfig.getInstance(context).getLocationAware()
            if (locationOld != null) {
                val lat = locationOld.getLat()
                val lng = locationOld.getLng()
                PreyLogger.d(
                    "AWARE lat:" + LocationUpdatesService.round(lat) + " lng:" + LocationUpdatesService.round(
                        lng
                    )
                )
                if (lat == 0.0 || lng == 0.0) {
                    PreyLogger.d("AWARE is zero")
                    return
                }
                val mGeofenceList: MutableList<Geofence> = ArrayList()
                mGeofenceList.add(
                    Geofence.Builder()
                        .setRequestId(GEO_AWARE_NAME)
                        .setCircularRegion(lat, lng, radius.toFloat())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setLoiteringDelay(loiteringDelay)
                        .setNotificationResponsiveness(notificationResponsiveness)
                        .build()
                )
                val builder = GeofencingRequest.Builder()
                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT or GeofencingRequest.INITIAL_TRIGGER_ENTER)
                builder.addGeofences(mGeofenceList)
                val geofencingRequest = builder.build()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //Added for android 12
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 0, Intent(
                            context,
                            AwareGeofenceReceiver::class.java
                        ), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    LocationServices.getGeofencingClient(context)
                        .addGeofences(geofencingRequest, pendingIntent)
                        .addOnSuccessListener {
                            PreyLogger.d(
                                "AWARE saveGeofence lat:" + LocationUpdatesService.round(
                                    lat
                                ) + " lng:" + LocationUpdatesService.round(lng)
                            )
                        }
                        .addOnFailureListener { e ->
                            PreyLogger.e(
                                "AWARE saveGeofence error: " + e.message,
                                e
                            )
                        }
                }
            } else {
                PreyLogger.d("AWARE locationOld is null")
            }
        } catch (e: Exception) {
            PreyLogger.e("AWARE error:" + e.message, e)
        }
    }

    @Throws(Exception::class)
    fun sendAware(context: Context, currentLocation: PreyLocation?): PreyLocation? {
        //get location
        val previousLocation = PreyConfig.getInstance(context).getLocationAware()
        val distanceAware = PreyConfig.getInstance(context).getDistanceAware()
        //TODO:SACAR DISTANCIA
        val mustSendAware = true//mustSendAware(context, previousLocation, currentLocation, distanceAware)
        //send aware
        return if (mustSendAware) {
            sendNowAware(context, currentLocation)
            currentLocation
        } else {
            null
        }
    }

    /**
     * Method to if location must send
     * @param ctx Context
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
        var sendAware = false
        if (previousLocation == null) {
            if (currentLocation != null) {
                sendAware = true
            }
        } else {
            if (currentLocation != null) {
                val distance = LocationUtil.distance(previousLocation, currentLocation)
                PreyLogger.d("AWARE distance:$distance > $distanceAware")
                if (distance > distanceAware) {
                    sendAware = true
                }
            }
        }
        return sendAware
    }

    @Throws(Exception::class)
    fun getSendNowAware(ctx: Context) {
        val currentLocation = LocationUtil.getLocation(ctx, null, false)
        PreyLogger.d("AWARE currentLocation:$currentLocation")
        sendNowAware(ctx, currentLocation)
    }

    /**
     * Method that sends the location
     * @param ctx Context
     * @param currentLocation  location
     * @return returns PreyHttpResponse
     */
    @Throws(Exception::class)
    fun sendNowAware(ctx: Context, currentLocation: PreyLocation?): PreyHttpResponse? {
        // Initialize response variable
        var preyResponse: PreyHttpResponse? = null
        if (currentLocation == null || currentLocation.getLat() == 0.0 || currentLocation.getLng() == 0.0) {
            // Log message if location is invalid
            PreyLogger.d("AWARE sendNowAware is zero")
            return preyResponse
        }
        // Get location aware status from config
        val isLocationAware = PreyConfig.getInstance(ctx).getAware()
        PreyLogger.d(String.format("AWARE sendNowAware isLocationAware:%s", isLocationAware))
        // Check if location aware is enabled
        if (isLocationAware) {
            val messageId: String? = null
            val reason: String? = null
            // Create JSON object for location wrapper
            val location = JSONObject()
            // Put location data into location wrapper
            location.put("location", createLocationData(currentLocation))
            // Set thread policy for Android versions > 9
            if (Build.VERSION.SDK_INT > 9) {
                val policy = ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
            }
            // Send location data using web services
            preyResponse = PreyWebServices.getInstance().sendLocation(ctx, location)
            // Check if response is not null
            if (preyResponse != null) {
                // Get status code and response from response object
                val statusCode = preyResponse.getStatusCode()
                val response = preyResponse.getResponseAsString()
                // Log status code and response
                PreyLogger.d(
                    String.format(
                        "AWARE statusCode:%s response:%s",
                        statusCode,
                        response
                    )
                )
                // Check if status code is HTTP_CREATED or HTTP_OK
                if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
                    // Set location aware data in config
                    PreyConfig.getInstance(ctx).setLocationAware(currentLocation)
                    PreyConfig.getInstance(ctx).setAwareTime()
                    // Check if response is "OK"
                    if ("OK" == response) {
                        // The date of the last location sent correctly is saved (yyyy-MM-dd )
                        PreyConfig.getInstance(ctx).setAwareDate(
                            PreyConfig.FORMAT_SDF_AWARE.format(Date())
                        )
                        // Log location aware data
                        PreyLogger.d(
                            String.format(
                                "AWARE sendNowAware:%s",
                                currentLocation.toString()
                            )
                        )
                    }
                }
            }
        }
        return preyResponse
    }

    private fun createLocationData(location: PreyLocation): JSONObject {
        val json = JSONObject()
        json.put("lat", location.getLat())
        json.put("lng", location.getLng())
        json.put("accuracy", roundAccuracy(location.getAccuracy().toDouble()))
        json.put("method", location.getMethod() ?: "native")
        return json
    }

    private fun roundAccuracy(accuracy: Double): Double {
        return Math.round(accuracy * 100.0) / 100.0
    }


    companion object {
        var GEO_AWARE_NAME: String = "AWARE"

        private var instance: AwareController? = null
        fun getInstance(): AwareController {
            if (instance == null) {
                instance = AwareController()
            }
            return instance!!
        }
    }
}