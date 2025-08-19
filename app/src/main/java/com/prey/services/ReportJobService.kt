/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.text.format.DateUtils

import com.prey.PreyConfig
import com.prey.actions.report.ReportService
import com.prey.PreyLogger

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportJobService : JobService() {

    private fun doBackgroundWork(jobParameters: JobParameters?) {
        CoroutineScope(Dispatchers.IO).launch {
            ReportService().run(applicationContext)
            jobFinished(jobParameters, false)
        }
    }

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("REPORT onStartJob")
        doBackgroundWork(jobParameters)
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        PreyLogger.d("REPORT onStopJob")
        jobFinished(jobParameters, false)
        return false
    }

    companion object {
        private const val JOB_ID = 124

        fun scheduleJob(context: Context) {
            var jobScheduler: JobScheduler? = null
            jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            val builder =
                JobInfo.Builder(1, ComponentName(context, ReportJobService::class.java.name))
            builder.setPeriodic(30 * DateUtils.MINUTE_IN_MILLIS)
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            builder.setRequiresDeviceIdle(false)
            val resulCode = jobScheduler.schedule(builder.build())
            if (resulCode == JobScheduler.RESULT_SUCCESS) {
                PreyLogger.d("REPORT resulCode success")
                PreyConfig.getInstance(context).setLastEvent("reportJobService_success")
            } else {
                PreyLogger.d("REPORT resulCode failed")
                PreyConfig.getInstance(context).setLastEvent("reportJobService_failed")
            }
        }

        fun cancelJob(context: Context) {
            var jobScheduler: JobScheduler? = null
            jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
        }
    }

}