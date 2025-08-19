/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.workers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices

import com.prey.PreyConfig
import com.prey.actions.location.PreyLocation
import com.prey.PreyLogger
import com.prey.actions.aware.AwareController
import com.prey.actions.location.LocationUtil

/**
 * A CoroutineWorker subclass responsible for retrieving the device's location.
 *
 * @param context The application context.
 * @param params The WorkerParameters instance.
 */
class PreyGetLocationWorker
    (
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Override the doWork function to perform the location retrieval task.
     *
     * @return Result The result of the work execution.
     */
    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        // Log a debug message to indicate that the location retrieval task has started.
        PreyLogger.d("AWARE PreyGetLocationWorker doWork_____")
        // Check if the location permission is granted.
        if (!hasLocationPermission()) {
            // If permission is not granted, return a failure result.
            PreyLogger.d("AWARE PreyGetLocationWorker hasLocationPermission")
            return Result.failure()
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    PreyLogger.d(
                        "AWARE PreyGetLocationWorker last lat_ :${
                            LocationUtil.round(
                                location.latitude
                            )
                        } lng:${
                            LocationUtil.round(
                                location.longitude
                            )
                        } acc:${
                            LocationUtil.round(
                                location.accuracy.toDouble()
                            )
                        }"
                    )
                    val preyLocation = PreyLocation(location)
                    PreyConfig.getInstance(context).setLocation(preyLocation)
                    PreyConfig.getInstance(context).setLocationAware(preyLocation)
                    AwareController.getInstance().sendAware(context, preyLocation)
                }
            }

        // Return a success result.
        return Result.success()
    }

    /**
     * Check if the location permission is granted.
     *
     * @return Boolean True if permission is granted, false otherwise.
     */
    private fun hasLocationPermission(): Boolean {
        // Check the permission status using the ActivityCompat.checkSelfPermission function.
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

}