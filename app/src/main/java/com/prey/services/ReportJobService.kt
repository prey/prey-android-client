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
import com.prey.actions.report.ReportService
import com.prey.PreyLogger

@TargetApi(21)
class ReportJobService : JobService() {
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("REPORT onStartJob")
        Thread {
            ReportService().run(applicationContext)
            val needsReschedule = false
            jobFinished(jobParameters, false)
        }.start()
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("REPORT onStopJob")
        jobFinished(jobParameters, false)
        return false
    }

    companion object {
        private const val JOB_ID = 124

        fun schedule(context: Context) {
            var jobScheduler: JobScheduler? = null
            jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            val builder = JobInfo.Builder(1, ComponentName(context, ReportJobService::class.java.name))
            builder.setPeriodic(30 * DateUtils.MINUTE_IN_MILLIS)
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            builder.setRequiresDeviceIdle(false)
            val resulCode = jobScheduler.schedule(builder.build())
            if (resulCode == JobScheduler.RESULT_SUCCESS) {
                PreyLogger.d("REPORT resulCode success")
            } else {
                PreyLogger.d("REPORT resulCode failed")
            }
        }

        fun cancel(context: Context) {
            var jobScheduler: JobScheduler? = null
            jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
        }
    }
}