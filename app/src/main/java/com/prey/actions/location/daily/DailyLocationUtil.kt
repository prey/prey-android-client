/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.prey.PreyLogger
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Utility object for scheduling and managing daily location tracking tasks.
 *
 * This utility uses [WorkManager] to enqueue a periodic background job that runs
 * once every 24 hours, specifically targeting an execution time around 00:30 AM.
 * It ensures that location checks are performed consistently while respecting
 * system constraints like network connectivity.
 */
object DailyLocationUtil {

    const val DAILY_LOCATION_WORK_ONE_TIME = "daily_location_work_one_time"
    const val DAILY_LOCATION_WORK_PERIODIC = "daily_location_work_periodic"

    fun enqueueDailyCheck(context: Context) {
        //Check if you need to send today
        sendToday(context)
        //Enqueue for the next day
        enqueueForTheNextDay(context)
    }

    /**
     * Enqueues a one-time background task to report the current location if it hasn't
     * been reported yet today.
     *
     * This function checks the [DailyStore] to verify the daily execution status.
     * If no report has been sent, it schedules a [DailyLocationWorker] through
     * [WorkManager] with a requirement for an active network connection.
     *
     * @param context The application context used to check execution history and
     *                access the WorkManager instance.
     */
    fun sendToday(context: Context) {
        val wasSentToday = DailyStore.wasSentToday(context)
        PreyLogger.d("DailyLocationUtil sendToday wasSentToday:${wasSentToday}")
        if (!wasSentToday) {
            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyLocationWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                DAILY_LOCATION_WORK_ONE_TIME, //a unique name to identify the task
                ExistingWorkPolicy.KEEP,   //maintains the task if there is already one glued
                dailyWorkRequest
            )
        }
    }

    /**
     * Schedules a periodic background task to report the device's location every 24 hours.
     *
     * This method configures a [PeriodicWorkRequest] that targets execution around 00:30 AM
     * by calculating an initial delay. It utilizes [ExistingPeriodicWorkPolicy.KEEP] to ensure
     * that existing schedules are maintained without resetting the timer. The task is
     */
    fun enqueueForTheNextDay(context: Context) {
        val locationRequest = PeriodicWorkRequestBuilder<DailyLocationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayUntil0030(), TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                30,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_LOCATION_WORK_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP, //It keeps the task if it already exists so as not to reset the delay.
            locationRequest
        )
    }

    /**
     * Calculates the delay in milliseconds from the current time until the next 00:30 AM.
     *
     * If the current time is already past 00:30 AM today, the delay is calculated
     * for 00:30 AM of the following day. This value is typically used for setting
     * the initial delay in a [WorkManager] request.
     *
     * @return The delay in milliseconds until the next occurrence of 00:30 AM.
     */
    fun calculateDelayUntil0030(): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }
        //If it's already past 00:30 today, schedule for tomorrows
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        return dueDate.timeInMillis - currentDate.timeInMillis
    }

}