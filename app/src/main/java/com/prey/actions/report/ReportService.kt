/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.report

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.prey.actions.HttpDataService
import com.prey.actions.location.LocationUpdatesService
import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.net.http.EntityFile
import com.prey.net.PreyWebServices
import com.prey.receivers.AlarmReportReceiver
import com.prey.util.ClassUtil
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

    fun run(ctx: Context): MutableList<HttpDataService>? {
        var interval = -1
        var listData: MutableList<HttpDataService>? = ArrayList()
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

            Thread(Runnable {
                val intentLocation = Intent(ctx, LocationUpdatesService::class.java)
                ctx.startService(intentLocation)
                ctx.stopService(intentLocation)
            }).start()

            PreyLogger.d("REPORT start:$interval")
            jsonArray = JSONArray()
            if (!exclude!!.contains("picture")) jsonArray.put("picture")

            if (!exclude!!.contains("access_points_list")) jsonArray.put("access_points_list")
            if (!exclude!!.contains("active_access_point")) jsonArray.put("active_access_point")
            if (!exclude!!.contains("location")) jsonArray.put("location")
            PreyLogger.d("REPORT jsonArray:$jsonArray")
            try {
                val listActions: MutableList<ActionResult> = ArrayList()
                for (i in 0 until jsonArray.length()) {
                    if (PreyConfig.getInstance(ctx).isMissing()) {
                        val nameAction = jsonArray.getString(i)
                        PreyLogger.d("REPORT start nameAction:$nameAction")
                        val methodAction = "report"
                        val parametersAction: JSONObject? = null
                        listData = ClassUtil.getInstance().execute(
                            ctx,
                            listActions,
                            nameAction,
                            methodAction,
                            parametersAction,
                            listData
                        )
                        PreyLogger.d("REPORT stop nameAction:$nameAction")
                    }
                }
            } catch (e: Exception) {
                PreyLogger.e("REPORT error:" + e.message, e)
            }
            var parms = 0

                var i = 0
                while (listData != null && i < listData.size) {
                    val httpDataService = listData[i]
                    parms = parms + httpDataService.getDataAsParameters().size
                    PreyLogger.d("REPORT ____params size:"+httpDataService.getDataAsParameters().size)
                    PreyLogger.d("REPORT ____params:"+httpDataService.getDataAsParameters().toString())
                    PreyLogger.d("REPORT ____files size:"+httpDataService.getEntityFiles().size)
                    if (httpDataService.getEntityFiles() != null) {
                        for (j in 0 until httpDataService.getEntityFiles().size) {
                            val entity: EntityFile = httpDataService.getEntityFiles()[j]
                            if (entity != null && entity.getLength() > 0) {
                                parms = parms + 1
                            }
                        }
                    }
                    i++
                }

            PreyLogger.d("REPORT ____params__ size:"+listData!!.size)
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
            PreyLogger.e("REPORT error:" + e.message, e)
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