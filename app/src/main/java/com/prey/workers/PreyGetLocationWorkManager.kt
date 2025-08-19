/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.workers

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

import com.prey.PreyLogger

/**
 * A manager class responsible for handling location-related work requests.
 */
class PreyGetLocationWorkManager private constructor() {

    /**
     * Schedules a one-time work request to retrieve the device's location.
     *
     * @param context The application context.
     */
    fun getLocationWork(context: Context) {
        // Log a debug message to indicate that the location work request has been initiated.
        PreyLogger.i("AWARE getLocation locationWork")
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_NAME)
        // Create a PeriodicWorkRequest builder
        val builder = OneTimeWorkRequestBuilder<PreyGetLocationWorker>()
            .addTag(WORK_NAME).setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
        // Build the PeriodicWorkRequest
        val workRequest: OneTimeWorkRequest = builder.build()
        // Enqueue the work request
        workManager.enqueue(workRequest)
    }

    /**
     * Companion object to provide a singleton instance of the PreyLocationWorkManager class.
     */
    companion object {
        const val WORK_NAME: String = "prey_get_location_work_manager"

        // The singleton instance of the PreyLocationWorkManager class.
        private var instance: PreyGetLocationWorkManager? = null

        /**
         * Returns the singleton instance of the PreyLocationWorkManager class.
         *
         * If the instance is null, it creates a new instance and assigns it to the instance variable.
         *
         * @return The singleton instance of the PreyLocationWorkManager class.
         */
        fun getInstance(): PreyGetLocationWorkManager =
            instance ?: PreyGetLocationWorkManager().also { instance = it }
    }

}