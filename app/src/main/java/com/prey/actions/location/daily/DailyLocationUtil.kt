/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Utility object for managing background location tasks.
 *
 * This utility handles the scheduling of daily location checks using [WorkManager],
 * ensuring that the task is executed under appropriate network conditions.
 */
object DailyLocationUtil {

    /**
     * Enqueues a unique one-time work request to perform a daily location check.
     *
     * This task is scheduled using [WorkManager] with a constraint requiring an active
     * network connection. If a task with the same name is already pending, it will
     * be kept and the new request will be ignored.
     *
     * @param context The application context used to initialize WorkManager.
     */
    fun enqueueDailyCheck(context: Context) {
        val request = OneTimeWorkRequestBuilder<DailyLocationWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "daily_location_work",
                ExistingWorkPolicy.KEEP,
                request
            )
    }

}