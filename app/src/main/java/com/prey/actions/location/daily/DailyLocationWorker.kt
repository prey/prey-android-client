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
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.prey.PreyLogger
import com.prey.PreyUtilsKt
import com.prey.net.PreyWebServicesKt

/**
 * A background worker responsible for periodically collecting and reporting the device's location.
 *
 */
class DailyLocationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        PreyLogger.d("DailyLocationWorker doWork")
        val wasSentToday = DailyStore.wasSentToday(applicationContext)
        PreyLogger.d("DailyLocationWorker __ wasSentToday:${wasSentToday}")
        if (wasSentToday) {
            return Result.success()
        }
        //Verify Permissions
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.retry() //Retry according to the defined policy
        }
        //Verify Connection
        if (!PreyUtilsKt.isNetworkAvailable(applicationContext)) {
            return Result.retry() //Retry according to the defined policy
        }
        //Obtain Location with Good Accuracy
        val location = DailyLocationProvider.fetchPreciseLocation(applicationContext)
        return if (location != null && location.accuracy < 75) { //Example: less than 75 meters
            val success = sendLocationToServer(location)
            if (success) {
                DailyStore.markSent(applicationContext)
                Result.success() //Task completed, please wait for the next daily cycle
            } else {
                Result.retry() //Server error, please try again in 30 minutes
            }
        } else {
            Result.retry() //There is no good accuracy yet
        }
    }

    /**
     * Sends the collected location data to the Prey web services.
     *
     * This method attempts to transmit the [location] object to the server and
     * handles potential network-related exceptions such as timeouts or DNS failures.
     *
     * @param location The device location to be reported.
     * @return `true` if the server successfully received the location, `false` otherwise.
     */
    private fun sendLocationToServer(location: Location): Boolean {
        return try {
            val sent = PreyWebServicesKt.sendDailyLocation(applicationContext, location)
            PreyLogger.d("DailyLocationWorker sent:${sent}")
            sent
        } catch (e: Exception) {
            //Timeouts or DNS failures come in here
            false
        }
    }

}