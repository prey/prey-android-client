/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.os.Build

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.observer.ActionResult
import com.prey.actions.report.ReportScheduled
import com.prey.json.UtilJson
import com.prey.services.ReportJobService

import org.json.JSONObject
import java.util.Calendar
import java.util.Date

/**
 * This class represents a Report and provides methods to get, stop, and validate reports.
 */
class Report {

    /**
     * Retrieves a report based on the provided parameters.
     *
     * @param context The application context.
     * @param actionResults A list of action results.
     * @param parameters A JSONObject containing the report parameters.
     */
    fun get(context: Context, actionResults: List<ActionResult>?, parameters: JSONObject?) {
        var jobId: String? = null
        try {
            jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
            PreyLogger.d("jobId:${jobId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var reason: String? = null
        if (jobId != null && "" != jobId) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        val lastReportStartDate = Date().time
        PreyLogger.d("____lastReportStartDate:${lastReportStartDate}")
        PreyConfig.getInstance(context).setLastReportStartDate(lastReportStartDate)
        PreyConfig.getInstance(context).setMissing(true)
        var interval = 0
        try {
            interval = UtilJson.getIntValue(parameters, "interval")
            PreyLogger.d("interval:${interval}")
        } catch (e: java.lang.Exception) {
            interval = 0
        }
        var exclude: String? = ""
        try {
            exclude = UtilJson.getStringValue(parameters, "exclude")
            PreyLogger.d("exclude:${exclude}")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:${messageId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyConfig.getInstance(context).setIntervalReport("$interval");
        try {
            PreyConfig.getInstance(context).setExcludeReport(exclude!!);
        } catch (Exception: Exception) {
            PreyConfig.getInstance(context).setExcludeReport("");
        }
        PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
            context,
            "processed",
            messageId,
            UtilJson.makeMapParam("get", "report", "started", reason)
        )
        PreyLogger.d("________start ReportScheduled")
        ReportScheduled.getInstance(context).run()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            ReportJobService.scheduleJob(context)
        }
    }

    /**
     * Stops the report.
     *
     * @param context The application context.
     * @param actionResults A list of action results.
     * @param parameters A JSONObject containing the report parameters.
     */
    fun stop(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        PreyLogger.d("________stop Report")
        ReportScheduled.getInstance(context).reset()
        PreyConfig.getInstance(context).setMissing(false)
        PreyConfig.getInstance(context).setIntervalReport("")
        PreyConfig.getInstance(context).setExcludeReport("")
        ReportJobService.cancelJob(context)
    }

    /**
     * Checks if the report is valid.
     *
     * @param context The application context.
     * @return True if the report is valid, false otherwise.
     */
    fun isValid(context: Context): Boolean {
        val lastReportStartDate = PreyConfig.getInstance(context).getLastReportStartDate()
        PreyLogger.d("last:${lastReportStartDate}")
        if (lastReportStartDate != 0L) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = lastReportStartDate
            cal.add(Calendar.MINUTE, 1)
            val timeMore = cal.timeInMillis
            PreyLogger.d("timM:${timeMore}")
            val nowDate = Date()
            val now = nowDate.time
            PreyLogger.d("now_:${now}")
            PreyLogger.d("now>=timeMore:${(now >= timeMore)}")
            return (now >= timeMore)
        }
        return true
    }

    /**
     * Runs the report with the specified interval.
     *
     * @param context The application context.
     * @param intervalReport The interval at which the report should be run.
     */
    fun run(context: Context, intervalReport: Int) {
        try {
            val parameters = JSONObject()
            parameters.put("interval", intervalReport)
            Report().get(context, null, parameters)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

}