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

import com.prey.beta.actions.PreyBetaActionsRunner

import java.util.concurrent.TimeUnit

/**
 * This class represents a worker that starts a periodic worker to run every 15 minutes, with a 5 minute flex period.
 */
class PreyReportWorkManager
/**
 * Private constructor to prevent instantiation of this class.
 */
private constructor() {
    /**
     * Starts a periodic worker to run every 15 minutes, with a 5 minute flex period.
     * @param context the application context
     */
    fun actionsWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_NAME)
        // Create a PeriodicWorkRequest builder
        val builder: PeriodicWorkRequest.Builder = PeriodicWorkRequest.Builder(
            PreyReportWorker::class.java,
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
        PreyBetaActionsRunner.getInstance(context).getInstructionsCoroutine(context, false)
    }

    companion object {
        const val WORK_NAME: String = "prey_report_work_manager"
        private var instance: PreyReportWorkManager? = null
        fun getInstance(): PreyReportWorkManager {
            if (instance == null) {
                instance = PreyReportWorkManager()
            }
            return instance!!
        }
    }

}