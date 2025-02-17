/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.workers

import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.prey.PreyLogger
import java.util.concurrent.TimeUnit

/**
 * A manager class responsible for handling location-related work requests.
 */
class PreyLocationWorkManager private constructor() {

    /**
     * Schedules a one-time work request to retrieve the device's location.
     *
     * @param context The application context.
     */
    fun locationWork(context: Context) {
        // Log a debug message to indicate that the location work request has been initiated.
        PreyLogger.i("AWARE PreyLocationWorkManager locationWork")
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_NAME)
        // Create a PeriodicWorkRequest builder
        val builder: PeriodicWorkRequest.Builder = PeriodicWorkRequest.Builder(
            PreyLocationWorker::class.java,
            15,
            TimeUnit.MINUTES,
            5,
            TimeUnit.MINUTES
        )
            .addTag(WORK_NAME)
        // Build the PeriodicWorkRequest
        val workRequest: PeriodicWorkRequest = builder.build()
        // Enqueue the work request
        workManager.enqueue(workRequest)
    }

    /**
     * Companion object to provide a singleton instance of the PreyLocationWorkManager class.
     */
    companion object {
        const val WORK_NAME: String = "prey_location_work_manager"
        // The singleton instance of the PreyLocationWorkManager class.
        private var instance: PreyLocationWorkManager? = null

        /**
         * Returns the singleton instance of the PreyLocationWorkManager class.
         *
         * If the instance is null, it creates a new instance and assigns it to the instance variable.
         *
         * @return The singleton instance of the PreyLocationWorkManager class.
         */
        fun getInstance(): PreyLocationWorkManager =
            instance ?: PreyLocationWorkManager().also { instance = it }
    }
}