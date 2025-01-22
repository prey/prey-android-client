/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.workers.kotlin

import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.prey.workers.IncrementWorker
import java.util.concurrent.TimeUnit

/**
 * This class represents a worker that starts a periodic worker to run every 15 minutes, with a 5 minute flex period.
 */
class PreyWorker
/**
 * Private constructor to prevent instantiation of this class.
 */
private constructor() {
    /**
     * Starts a periodic worker to run every 15 minutes, with a 5 minute flex period.
     * @param context the application context
     */
    fun startPeriodicWork(context: Context) {
        val workManager = WorkManager.getInstance(context!!)
        workManager.cancelAllWorkByTag(com.prey.workers.PreyWorker.INCREMENT_WORK_NAME)
        // Create a PeriodicWorkRequest builder
        val builder: PeriodicWorkRequest.Builder = PeriodicWorkRequest.Builder(
            IncrementWorker::class.java,
            15,
            TimeUnit.MINUTES,
            5,
            TimeUnit.MINUTES
        )
            .addTag(com.prey.workers.PreyWorker.INCREMENT_WORK_NAME)
        // Build the PeriodicWorkRequest
        val workRequest: PeriodicWorkRequest = builder.build()
        // Enqueue the work request
        workManager.enqueue(workRequest)
    }

    companion object {

        const val INCREMENT_WORK_NAME: String = "prey_increment_work"

        private var INSTANCE: PreyWorker? = null


        fun getInstance(): PreyWorker {

            if (INSTANCE == null) {

                INSTANCE = PreyWorker()
            }
            return INSTANCE!!
        }
    }
}