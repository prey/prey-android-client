/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.text.format.DateUtils

import com.prey.actions.aware.AwareController
import com.prey.PreyLogger

/**
 * A JobService that handles location awareness.
 */
@TargetApi(21)
class AwareJobService : JobService() {

    /**
     * Called when the job is scheduled.
     *
     * @param jobParameters the parameters of the job.
     * @return true if the job should be rescheduled, false otherwise.
     */
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("AWARE onStartJob")
        Thread {
            AwareController.getInstance().initLastLocation(applicationContext)
            val needsReschedule = false
            jobFinished(jobParameters, false)
        }.start()
        return true
    }

    /**
     * Called when the job is stopped.
     *
     * @param jobParameters the parameters of the job.
     * @return false if the job should not be rescheduled, true otherwise.
     */
    override fun onStopJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("AWARE onStopJob")
        jobFinished(jobParameters, false)
        return false
    }

    /**
     * Schedules the AwareJobService to run periodically.
     *
     * @param context the context.
     */
    fun schedule(context: Context) {
        var jobScheduler: JobScheduler? = null
        jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val builder = JobInfo.Builder(
            JOB_ID, ComponentName(
                context,
                AwareJobService::class.java.name
            )
        )
        builder.setPeriodic(20 * DateUtils.MINUTE_IN_MILLIS)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
        builder.setRequiresDeviceIdle(false)
        val resulCode = jobScheduler.schedule(builder.build())
        if (resulCode == JobScheduler.RESULT_SUCCESS) {
            PreyLogger.d("AWARE resulCode success")
        } else {
            PreyLogger.d("AWARE resulCode failed")
        }
    }

    /**
     * Cancels the scheduled AwareJobService.
     *
     * @param context the context.
     */
    fun cancel(context: Context) {
        var jobScheduler: JobScheduler? = null
        jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(JOB_ID)
    }

    companion object {
        private const val JOB_ID = 123
    }
}