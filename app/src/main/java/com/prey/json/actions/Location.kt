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
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.json.CommandTarget
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
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
object Location : CommandTarget, BaseAction() {

    // It is recommended to use a Scope linked to the app or service lifecycle
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private const val TARGET = "location"
    const val LOCATION_TIMEOUT = 30_000L // 30 maximum waiting time seconds

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_GET -> getCoroutine(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    fun getCoroutine(context: Context, options: JSONObject) {
        scope.launch { get(context, options) }
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
    suspend fun get(context: Context, options: JSONObject) {
        val messageId = options.optString(PreyConfig.MESSAGE_ID, null)
        val jobId = options.optString(PreyConfig.JOB_ID, null)
        val reason = jobId?.let { "{\"device_job_id\":\"$it\"}" }
        PreyWebServicesKt.notify(context, CMD_GET, TARGET, STATUS_STARTED, reason, messageId)
        PreyLogger.d("Location get 3")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PreyLogger.d("Permission denied for location")
            PreyWebServicesKt.notify(context, CMD_GET, TARGET, STATUS_FAILED, "permission_denied")
            return
        }
        try {
            // We added a timeout so that the coroutine doesn't remain suspended forever if the GPS fails.
            val location = withTimeoutOrNull(LOCATION_TIMEOUT) {
                getLocation(context)
            }
            if (location != null) {
                PreyLogger.d("Location obtained: ${location.latitude}, ${location.longitude}")
                PreyWebServicesKt.doSendLocation(context, location, false)
                PreyWebServicesKt.notify(context, CMD_GET, TARGET, STATUS_STOPPED)
            } else {
                PreyLogger.d("Location could not be obtained (Timeout or Null)")
                PreyWebServicesKt.notify(context, CMD_GET, TARGET, STATUS_FAILED, "location_null_or_timeout")
            }
        } catch (e: Exception) {
            PreyLogger.e("Error in localization process: ${e.message}", e)
            PreyWebServicesKt.notify(context, CMD_GET, TARGET, STATUS_FAILED, e.message)
        }
    }

    private lateinit var fusedClient: FusedLocationProviderClient

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    suspend fun getLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                0
            ).setMaxUpdates(1).build()
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