/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.prey.PreyLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import androidx.core.content.edit

/**
 * Utility object for handling location-related operations, including retrieving the last known
 * location, persisting location data to shared preferences, and calculating distances.
 *
 * This utility integrates with Google Play Services [LocationServices] to fetch coordinates
 * and provides helper methods for coordinate formatting and storage.
 */
object LocationUtil {

    const val LAT = "lat"
    const val LNG = "lng"
    const val ACC = "accuracy"
    const val METHOD = "method"
    const val FORCE = "force"

    /**
     * Retrieves the last known location of the device using the Fused Location Provider API.
     *
     * This function asynchronously attempts to fetch the most recent location available.
     * If the location is successfully retrieved, it is passed to the [onResult] callback.
     * In case of failure or if the necessary permissions are not granted (SecurityException),
     * the callback is invoked with a `null` value.
     *
     * @param context The [Context] used to initialize the FusedLocationProviderClient.
     * @param onResult A callback function that receives the retrieved [Location] object or `null`.
     */
    fun lastLocation(
        context: Context,
        onResult: (Location?) -> Unit
    ) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    onResult(location)
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } catch (e: SecurityException) {
            onResult(null)
        }
    }

    /**
     * Attempts to retrieve the current or last known location of the device.
     *
     * This method clears previous cached data and initiates an asynchronous request to fetch the
     * device's last known location. It then performs a polling loop (blocking the current thread)
     * for up to 6 seconds (30 iterations of 200ms) to wait for the location to be saved to preferences.
     *
     * @param context The application or activity context.
     * @return A [PreyLocation] object if a location is successfully retrieved within the timeout period,
     *         or `null` if the location could not be determined or a timeout occurred.
     */
    fun getLocation(
        context: Context
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        clear(context)
        CoroutineScope(Dispatchers.IO).launch {
            lastLocation(context) { location ->
                if (location != null) {
                    save(context, location)
                }
            }
        }
        var i = 0
        do {
            val location = get(context)
            if (location != null) {
                preyLocation = PreyLocation(location)
                break
            }
            Thread.sleep(200)
            i += 1
        } while (i < 30)
        return preyLocation
    }

    private const val GET_PREF = "location_get_pref"

    /**
     * Saves the provided location coordinates and accuracy into private shared preferences.
     *
     * @param context The application context used to access shared preferences.
     * @param location The [Location] object containing the latitude, longitude, and accuracy to be stored.
     */
    fun save(context: Context, location: Location) {
        val prefs = context.getSharedPreferences(GET_PREF, Context.MODE_PRIVATE)
        prefs.edit {
            putFloat(LAT, location.latitude.toFloat())
                .putFloat(LNG, location.longitude.toFloat())
                .putFloat(ACC, location.accuracy)
        }
    }

    /**
     * Retrieves the last saved location from the shared preferences.
     *
     * @param context The application context used to access shared preferences.
     * @return A [Location] object containing the stored latitude, longitude, and accuracy,
     *         or `null` if no location has been saved.
     */
    fun get(context: Context): Location? {
        val prefs = context.getSharedPreferences(GET_PREF, Context.MODE_PRIVATE)
        if (!prefs.contains(LAT)) return null
        val location = Location("saved")
        location.latitude = prefs.getFloat(LAT, 0f).toDouble()
        location.longitude = prefs.getFloat(LNG, 0f).toDouble()
        location.accuracy = prefs.getFloat(ACC, 0f)
        return location
    }

    /**
     * Clears the stored location data from SharedPreferences.
     *
     * @param context The application context used to access SharedPreferences.
     */
    fun clear(context: Context) {
        context.getSharedPreferences(GET_PREF, Context.MODE_PRIVATE)
            .edit { clear() }
    }

    /**
     * Calculates the distance in meters between two [PreyLocation] points.
     *
     * The result is rounded to the nearest whole number. If either location is null,
     * the function returns 0.0.
     *
     * @param locationOld The starting location.
     * @param locationNew The ending location.
     * @return The rounded distance between the two points in meters, or 0.0 if a location is missing.
     */
    fun distance(locationOld: PreyLocation?, locationNew: PreyLocation?): Double {
        if (locationOld != null && locationNew != null) {
            val locStart = Location("")
            locStart.setLatitude(locationNew.getLat())
            locStart.setLongitude(locationNew.getLng())
            val locEnd = Location("")
            locEnd.setLatitude(locationOld.getLat())
            locEnd.setLongitude(locationOld.getLng())
            return Math.round(locStart.distanceTo(locEnd)).toDouble()
        } else {
            return 0.0
        }
    }

    /**
     * Rounds a [Double] value to six decimal places.
     *
     * This method formats the input using a decimal pattern ("0.000000") and parses
     * it back into a [Double], ensuring precision is limited for location-related coordinates.
     *
     * @param value The double value to be rounded.
     * @return The value rounded to six decimal places, or 0.0 if an error occurs during parsing.
     */
    fun round(value: Double): Double {
        var finalValue = 0.0
        val df = DecimalFormat("0.000000")
        val format = df.format(value)
        try {
            finalValue = (df.parse(format) as kotlin.Double?)!!
        } catch (e1: Exception) {
            try {
                val finalValue2 = df.parse(format) as Long
                finalValue = finalValue2.toDouble()
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
        }
        return finalValue
    }

}