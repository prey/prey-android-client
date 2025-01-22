/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services.kotlin

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.text.format.DateUtils
import com.prey.actions.aware.kotlin.AwareController
import com.prey.kotlin.PreyLogger

@TargetApi(21)
class AwareJobService : JobService() {
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("AWARE onStartJob")
        Thread {
            AwareController.getInstance().init(applicationContext)
            val needsReschedule = false
            jobFinished(jobParameters, false)
        }.start()
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("AWARE onStopJob")
        jobFinished(jobParameters, false)
        return false
    }

    companion object {
        private const val JOB_ID = 123

        fun schedule(ctx: Context) {
            var jobScheduler: JobScheduler? = null
            jobScheduler = ctx.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            val builder = JobInfo.Builder(
                JOB_ID, ComponentName(
                    ctx,
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

        fun cancel(ctx: Context) {
            var jobScheduler: JobScheduler? = null
            jobScheduler = ctx.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
        }
    }
}