/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.report.kotlin

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.prey.actions.kotlin.HttpDataService
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPermission
import com.prey.net.kotlin.PreyWebServices
import com.prey.receivers.kotlin.AlarmReportReceiver
import com.prey.util.kotlin.ClassUtil
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Date

class ReportService : IntentService {
    constructor() : super("reportService")

    constructor(name: String?) : super(name)

    override fun onHandleIntent(intent: Intent?) {
        run(this)
        stopSelf()
    }

    fun run(ctx: Context): List<HttpDataService>? {
        var interval = -1
        var listData: List<HttpDataService>? = ArrayList()
        try {
            PreyLogger.d("REPORT _____________start ReportService")
            interval = try {
                PreyConfig.getInstance(ctx).getIntervalReport()!!.toInt()
            } catch (ee: Exception) {
                10
            }
            //If it is Android 12 and you have alarm permission, run at the exact time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (PreyPermission.canScheduleExactAlarms(ctx)) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = Date().time
                    calendar.add(Calendar.MINUTE, interval)
                    val intent = Intent(ctx, AlarmReportReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        ctx,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    val alarmMgr = ctx.getSystemService(ALARM_SERVICE) as AlarmManager
                    alarmMgr.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
            val exclude = PreyConfig.getInstance(ctx).getExcludeReport()
            var jsonArray = JSONArray()
            PreyLogger.d("REPORT start:$interval")
            jsonArray = JSONArray()
            if (!exclude!!.contains("picture")) jsonArray.put("picture")
            if (!exclude!!.contains("location")) jsonArray.put("location")
            if (!exclude!!.contains("access_points_list")) jsonArray.put("access_points_list")
            if (!exclude!!.contains("active_access_point")) jsonArray.put("active_access_point")
            try {
                val lista: List<ActionResult> = ArrayList()
                for (i in 0 until jsonArray.length()) {
                    if (PreyConfig.getInstance(ctx).isMissing()) {
                        val nameAction = jsonArray.getString(i)
                        PreyLogger.d("nameAction:$nameAction")
                        val methodAction = "report"
                        val parametersAction: JSONObject? = null
                        listData = ClassUtil.getInstance().execute(
                            ctx,
                            lista,
                            nameAction,
                            methodAction,
                            parametersAction,
                            listData
                        )
                    }
                }
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            var parms = 0
            var i = 0
            while (listData != null && i < listData.size) {
                val httpDataService = listData[i]
                parms = parms + httpDataService.getDataAsParameters().size
                if (httpDataService.getEntityFiles() != null) {
                    for (j in httpDataService.getEntityFiles().indices) {
                        val entity = httpDataService.getEntityFiles()[j]
                        if (entity != null && entity.length > 0) {
                            parms = parms + 1
                        }
                    }
                }
                i++
            }
            if (PreyConfig.getInstance(ctx).isMissing()) {
                if (parms > 0) {
                    val response = PreyWebServices.getInstance().sendPreyHttpReport(ctx, listData)
                    if (response != null) {
                        PreyConfig.getInstance(ctx).setLastEvent("report_send")
                        PreyLogger.d("REPORT response.getStatusCode():" + response.getStatusCode())
                        if (409 == response.getStatusCode()) {
                            ReportScheduled.getInstance(ctx)!!.reset()
                            PreyConfig.getInstance(ctx).setMissing(false)
                            PreyConfig.getInstance(ctx).setIntervalReport("")
                            PreyConfig.getInstance(ctx).setExcludeReport("")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("error report:" + e.message, e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                "failed",
                null,
                UtilJson.makeMapParam("get", "report", "failed", e.message)
            )
        }
        return listData
    }
}