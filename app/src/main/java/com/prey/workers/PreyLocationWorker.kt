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
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.prey.PreyConfig

import com.prey.actions.location.PreyLocation
import com.prey.actions.location.PreyLocationManager
import com.prey.PreyLogger
import com.prey.actions.aware.AwareController

/**
 * A CoroutineWorker subclass responsible for retrieving the device's location.
 *
 * @param context The application context.
 * @param params The WorkerParameters instance.
 */
class PreyLocationWorker
    (
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // Initialize the FusedLocationProviderClient instance.
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Override the doWork function to perform the location retrieval task.
     *
     * @return Result The result of the work execution.
     */
    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        // Log a debug message to indicate that the location retrieval task has started.
        PreyLogger.d("AWARE PreyLocationWorker doWork")
        // Check if the location permission is granted.
        if (!hasLocationPermission()) {
            // If permission is not granted, return a failure result.
            return Result.failure()
        }
        AwareController.getInstance().initLastLocation(context)
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