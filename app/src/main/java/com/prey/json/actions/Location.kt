/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.json.CommandTarget
import com.prey.json.UtilJson
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Handles remote commands related to the device's geographical location.
 *
 * This class implements the [CommandTarget] interface to process commands
 * sent from the Prey panel. It is responsible for retrieving the device's
 * current location and reporting it back to the backend services.
 *
 * The primary command supported is "get", which triggers a one-time,
 * high-accuracy location request using the [FusedLocationProviderClient].
 * The process is asynchronous, managed by coroutines, and includes sending
 * "started" and "stopped" notifications to the backend to track the job's lifecycle.
 */
class Location : CommandTarget {

    override fun execute(context: Context, command: String, options: JSONObject): Any? {
        return when (command) {
            "get" -> get(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Asynchronously retrieves the device's current location and sends it to the Prey servers.
     *
     * This function is triggered by a "get" command for the "location" target. It operates on a background
     * thread using a coroutine.
     *
     * It performs the following steps:
     * 1. Extracts the `messageId` and `jobId` from the provided `options` JSON object.
     * 2. Notifies the Prey servers that the location retrieval process has started.
     * 3. Calls `getLocation()` to obtain the device's current location.
     * 4. If a location is successfully retrieved, it sends the location data to the Prey servers.
     * 5. Notifies the Prey servers that the location retrieval process has stopped.
     *
     * @param context The application context, used for accessing location services and network operations.
     * @param options A [JSONObject] containing command parameters, expected to include
     *                `PreyConfig.MESSAGE_ID` and `PreyConfig.JOB_ID`.
     */
    fun get(context: Context, options: JSONObject) {
        CoroutineScope(Dispatchers.IO).launch {
            var messageId: String? = null
            try {
                messageId = options.getString(PreyConfig.MESSAGE_ID)
                PreyLogger.d("messageId:${messageId}")
            } catch (e: java.lang.Exception) {
            }
            var reason: String? = null
            try {
                val jobId = options.getString(PreyConfig.JOB_ID)
                reason = "{\"device_job_id\":\"${jobId}\"}"
                PreyLogger.d("jobId:${jobId}")
            } catch (e: java.lang.Exception) {
            }
            PreyWebServicesKt.sendNotifyActions(
                context,
                UtilJson.makeJsonResponse("get", "location", "started"),
                messageId,
                reason
            )
            val location = getLocation(context)
            if (location != null) {
                PreyWebServicesKt.doSendLocation(context, location, false)
                PreyWebServicesKt.sendNotifyActions(
                    context,
                    UtilJson.makeJsonResponse("get", "location", "stopped")
                )
            }
        }
    }

    private lateinit var fusedClient: FusedLocationProviderClient
    private suspend fun getLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                0
            ).setMaxUpdates(1).build()
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.requestLocationUpdates(
                    request,
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            fusedClient.removeLocationUpdates(this)
                            cont.resume(result.lastLocation)
                        }
                    },
                    Looper.getMainLooper()
                )
            }
        }

}