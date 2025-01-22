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
import com.prey.beta.actions.kotlin.PreyBetaActionsRunner
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyScheduled
import com.prey.kotlin.PreyUtils

@TargetApi(21)
class PreyJobService : JobService() {
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("SCHEDULE onStartJob")
        try {
            PreyBetaActionsRunner.getInstructionsNewThread(applicationContext, null, false)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("SCHEDULE onStopJob")
        return true
    }

    companion object {
        private const val JOB_ID = 321

        fun getMinuteScheduled(ctx: Context): Int {
            var minutes = 15
            val isGooglePlayServicesAvailable = PreyUtils.isGooglePlayServicesAvailable(ctx)
            if (!isGooglePlayServicesAvailable) {
                minutes = PreyConfig.getInstance(ctx).getMinutesToQueryServer()
            }
            return minutes
        }

        fun schedule(ctx: Context) {
            val minutes = getMinuteScheduled(ctx)
            PreyLogger.d("SCHEDULE minuteScheduled:$minutes")
            if (minutes == 0) {
                PreyScheduled.getInstance(ctx)!!.reset()
                cancel(ctx)
            } else {
                PreyScheduled.getInstance(ctx)!!.run(minutes)
                var jobScheduler: JobScheduler? = null
                jobScheduler = ctx.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
                val builder = JobInfo.Builder(
                    JOB_ID, ComponentName(
                        ctx,
                        PreyJobService::class.java.name
                    )
                )
                builder.setPeriodic(minutes * DateUtils.MINUTE_IN_MILLIS)
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                builder.setRequiresDeviceIdle(false)
                val resulCode = jobScheduler.schedule(builder.build())
                if (resulCode == JobScheduler.RESULT_SUCCESS) {
                    PreyLogger.d("SCHEDULE resulCode success[$minutes]")
                    try {
                        PreyBetaActionsRunner.getInstructionsNewThread(ctx, null, false)
                    } catch (e: Exception) {
                    }
                } else {
                    PreyLogger.d("SCHEDULE resulCode failed")
                }
            }
        }

        fun cancel(ctx: Context) {
            PreyScheduled.getInstance(ctx)!!.reset()
            var jobScheduler: JobScheduler? = null
            jobScheduler = ctx.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
            PreyLogger.d("SCHEDULE cancel")
        }
    }
}
