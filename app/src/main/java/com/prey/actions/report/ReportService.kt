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
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.receivers.AlarmReportReceiver
import java.util.Calendar
import java.util.Date

/**
 * ReportService is an IntentService responsible for handling report-related tasks.
 */
class ReportService : IntentService {
    constructor() : super("reportService")

    constructor(name: String?) : super(name)

    /**
     * Handles the intent sent to this service.
     *
     * This method is called on the worker thread.
     *
     * @param intent The intent to handle.
     */
    override fun onHandleIntent(intent: Intent?) {
        run(this)
        stopSelf()
    }

    /**
     * Runs the report service and returns a list of HttpDataService objects.
     *
     * @param context The application context.
     * @return A list of HttpDataService objects, or null if an error occurs.
     */
    fun run(context: Context): MutableList<HttpDataService>? {
        var interval = -1
        var listData: MutableList<HttpDataService>? = ArrayList()
        try {
            PreyConfig.getInstance(context).setLastEvent("report_start")
            PreyLogger.d("REPORT _____________start ReportService")
            interval = try {
                PreyConfig.getInstance(context).getIntervalReport()!!.toInt()
            } catch (e: Exception) {
                10
            }
            //If it is Android 12 and you have alarm permission, run at the exact time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (PreyPermission.canScheduleExactAlarms(context)) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = Date().time
                    calendar.add(Calendar.MINUTE, interval)
                    val intent = Intent(context, AlarmReportReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager
                    alarmMgr.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
            listData = ReportAction().start(context, interval)
        } catch (e: Exception) {
            PreyLogger.e("REPORT error:${e.message}", e)
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                "failed",
                null,
                UtilJson.makeMapParam("get", "report", "failed", e.message)
            )
        }
        return listData
    }

}