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

import com.prey.PreyLogger
import com.prey.beta.actions.PreyBetaActionsRunner

import org.json.JSONObject

/**
 * A Worker class responsible for incrementing a value and handling AwareController initialization.
 */
class PreyActionsWorker
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
        PreyLogger.d("AWARE ACTIONS doWork")
        // Get the application context
        val context = applicationContext
        try {
            var jsonObject: List<JSONObject>? = null
            jsonObject = PreyBetaActionsRunner.getInstance(context).getInstructions(context, true)
            PreyLogger.d("AWARE runInstructions")
            if (jsonObject != null) {
                PreyBetaActionsRunner.getInstance(context).runInstructions(jsonObject)
            }
            return Result.success()
        } catch (e: NumberFormatException) {
            PreyLogger.e("----------Error IncrementWorker:${e.message}", e)
            return Result.failure()
        } catch (throwable: Throwable) {
            PreyLogger.e(
                "----------Error IncrementWorker:${throwable.message}",
                throwable
            )
            return Result.failure()
        }
    }

}