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
import com.prey.actions.report.ReportService
import com.prey.beta.actions.PreyBetaActionsRunner
import org.json.JSONObject


class PreyReportWorker

    (
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        // Log a debug message to indicate the start of the work
        PreyLogger.d("AWARE REPORT doWork")
        // Get the application context
        val context = applicationContext
        try {
            ReportService().run(applicationContext)
            return Result.success()
        } catch (e: NumberFormatException) {
            PreyLogger.e( "----------Error PreyReportWorker:${e.message}", e)
            return Result.failure()
        } catch (throwable: Throwable) {
            PreyLogger.e( "----------Error PreyReportWorker:${throwable.message}",   throwable )
            return Result.failure()
        }
    }
}