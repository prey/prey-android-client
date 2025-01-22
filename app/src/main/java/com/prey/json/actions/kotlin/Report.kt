/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.content.Context
import android.os.Build
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.actions.report.kotlin.ReportScheduled
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices
import com.prey.services.kotlin.ReportJobService
import org.json.JSONObject
import java.util.Calendar
import java.util.Date

class Report {
    fun get(ctx: Context, list: List<ActionResult>?, parameters: JSONObject?) {
        var jobId: String? = null
        try {
            jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID)
            PreyLogger.d(String.format("jobId:%s", jobId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var reason: String? = null
        if (jobId != null && "" != jobId) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        val lastReportStartDate = Date().time
        PreyLogger.d(String.format("____lastReportStartDate:%s", lastReportStartDate))
        PreyConfig.getInstance(ctx).setLastReportStartDate(lastReportStartDate)
        PreyConfig.getInstance(ctx).setMissing(true)
        var interval = 0
        try {
            interval = UtilJson.getInt(parameters, "interval")
            PreyLogger.d(String.format("interval:%s", interval))
        } catch (e: Exception) {
            interval = 0
        }
        var exclude: String? = ""
        try {
            exclude = UtilJson.getString(parameters, "exclude")
            PreyLogger.d(String.format("exclude:%s", exclude))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:s", e.message), e)
        }
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d(String.format("messageId:%s", messageId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        PreyConfig.getInstance(ctx).setIntervalReport("" + interval);
        PreyConfig.getInstance(ctx).setExcludeReport(exclude!!);
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
            ctx,
            "processed",
            messageId,
            UtilJson.makeMapParam("get", "report", "started", reason)
        )
        PreyLogger.d("________start ReportScheduled")
        ReportScheduled.getInstance(ctx)!!.run()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            ReportJobService.schedule(ctx)
        }
    }

    fun stop(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject?) {
        PreyLogger.d("________stop Report")
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d(String.format("messageId:%s", messageId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        ReportScheduled.getInstance(ctx)!!.reset()
        PreyConfig.getInstance(ctx).setMissing(false)
        PreyConfig.getInstance(ctx).setIntervalReport("")
        PreyConfig.getInstance(ctx).setExcludeReport("")
        ReportJobService.cancel(ctx)
    }

    fun valida(ctx: Context): Boolean {
        val lastReportStartDate = PreyConfig.getInstance(ctx).getLastReportStartDate()
        PreyLogger.d(String.format("last:%s", lastReportStartDate))
        if (lastReportStartDate != 0L) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = lastReportStartDate
            cal.add(Calendar.MINUTE, 1)
            val timeMore = cal.timeInMillis
            PreyLogger.d(String.format("timM:%d", timeMore))
            val nowDate = Date()
            val now = nowDate.time
            PreyLogger.d(String.format("now_:%d", now))
            PreyLogger.d(String.format("now>=timeMore:%d", (now >= timeMore)))
            return (now >= timeMore)
        }
        return true
    }

    companion object {
        fun run(ctx: Context, intervalReport: Int) {
            try {
                val parameters = JSONObject()
                parameters.put("interval", intervalReport)
                Report().get(ctx, null, parameters)
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
        }
    }
}