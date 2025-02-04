/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.prey.actions.aware.AwareController
import com.prey.PreyConfig
import com.prey.PreyLogger

/**
 * A Worker class responsible for incrementing a value and handling AwareController initialization.
 */
class IncrementWorker
/**
 * Constructor for IncrementWorker.
 *
 * @param context  the application context
 * @param params   the worker parameters
 */
    (
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    /**
     * Performs the work for this Worker.
     *
     * @return Result.success() if the work is successful, Result.failure() otherwise
     */
    override fun doWork(): Result {
        // Log a debug message to indicate the start of the work
        PreyLogger.d("AWARE WORK doWork")
        // Get the application context
        val context = applicationContext
        try {
            // Check if it's time to run the AwareController
            if (PreyConfig.getInstance(context).isTimeNextAware()) {
                // Create a new thread to run the AwareController initialization
                object : Thread() {
                    override fun run() {
                        // Initialize the AwareController
                        AwareController().init(context)
                    }
                }.start() // Start the thread
            }
            return Result.success()
        } catch (e: NumberFormatException) {
            PreyLogger.e(String.format("----------Error IncrementWorker:%s", e.message), e)
            return Result.failure()
        } catch (throwable: Throwable) {
            PreyLogger.e(
                String.format("----------Error IncrementWorker:%s", throwable.message),
                throwable
            )
            return Result.failure()
        }
    }
}