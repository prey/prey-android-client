/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.prey.PreyLogger
import com.prey.net.PreyWebServicesKt

/**
 * A [CoroutineWorker] responsible for periodic daily location updates.
 *
 * This worker ensures that the device's location is captured and reported to the server
 * once per day. It handles permission checks, location retrieval via [DailyLocationProvider],
 * and synchronizes the reported state using [DailyStore].
 *
 * @param context The application context.
 * @param params Parameters for the worker.
 */
class DailyLocationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val locationProvider: DailyLocationProvider = DailyLocationProvider(context)

    /**
     * Executes the daily location update task.
     *
     * This method performs the following checks and actions:
     * 1. Verifies if a location update has already been successfully sent today.
     * 2. Checks if the required location permissions ([Manifest.permission.ACCESS_FINE_LOCATION]) are granted.
     * 3. Attempts to retrieve the current device location via [locationProvider].
     * 4. Sends the location data using [sender].
     * 5. Marks the task as completed for the day in [DailyStore] upon a successful send.
     *
     * @return [Result.success] if already sent today, if permissions are missing, or if data is sent successfully.
     *         [Result.retry] if the location cannot be retrieved or if the sending process fails.
     */
    override suspend fun doWork(): Result {
        PreyLogger.d("DailyLocationWorker doWork")
        val wasSentToday = DailyStore.wasSentToday(applicationContext)
        PreyLogger.d("DailyLocationWorker wasSentToday:${wasSentToday}")
        if (wasSentToday) {
            return Result.success()
        }
        if( ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED){
            PreyLogger.d(" DailyLocationWorker not permission")
            return Result.success()
        }
        val location = locationProvider.getCurrentLocation()
            ?: return Result.retry()
        PreyLogger.d("DailyLocationWorker location:${location.latitude} ${location.longitude} ${location.accuracy}")
        val sent = PreyWebServicesKt.sendDailyLocation(applicationContext, location)
        PreyLogger.d("DailyLocationWorker sent:${sent}")
        return if (sent) {
            DailyStore.markSent(applicationContext)
            Result.success()
        } else {
            Result.retry()
        }
    }

}