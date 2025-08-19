/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.workers

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.aware.AwareController
import com.prey.actions.location.LocationUtil
import com.prey.actions.location.PreyLocation
import com.prey.actions.location.PreyLocationManager

/**
 * A CoroutineWorker subclass responsible for retrieving the device's location.
 *
 * @param context The application context.
 * @param param The WorkerParameters instance.
 */
class PreyLocationWorker
    (
    val context: Context,
    param: WorkerParameters
) : CoroutineWorker(context, param) {

    // Initialize the FusedLocationProviderClient instance.
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

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
        if (!LocationUtil.hasLocationPermission(context)) {
            // If permission is not granted, return a failure result.
            return Result.failure()
        }

        locationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                PreyLogger.d("AWARE PreyLocationWorker lastLocation New location: $location")
                sendLocation(PreyLocation(location))
            }
        }
        /*
      locationClient.getCurrentLocation(
          Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token,
      ).addOnSuccessListener { location ->
          location?.let {
              PreyLogger.d("AWARE PreyLocationWorker getCurrentLocation New location: $location")
              sendLocation(PreyLocation(location))
          }
      }*/
        // Return a success result.
        return Result.success()
    }

    private fun sendLocation(preyLocation: PreyLocation) {
        PreyLocationManager.getInstance().setLastLocation(preyLocation)
        PreyConfig.getInstance(context).setLocation(preyLocation)
        PreyConfig.getInstance(context).setLocationAware(preyLocation)
        AwareController.getInstance().sendAware(context, preyLocation)
    }

}