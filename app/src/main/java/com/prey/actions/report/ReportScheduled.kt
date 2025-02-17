/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.report

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.receivers.AlarmReportReceiver

/**
 * A utility class for scheduling and canceling report alarms.
 */
class ReportScheduled private constructor(var context: Context) {

    // The AlarmManager instance used for scheduling alarms.
    private var alarmMgr: AlarmManager? = null

    /**
     * Starts the report scheduling process.
     *
     * This method retrieves the report interval from the PreyConfig instance,
     * creates a PendingIntent for the AlarmReportReceiver, and schedules
     * an alarm using the AlarmManager.
     */
    fun run() {
        try {
            val minute = PreyConfig.getInstance(context).getIntervalReport()!!.toInt()
            PreyLogger.d("----------ReportScheduled start minute:$minute")
            val intent = Intent(context, AlarmReportReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                PreyLogger.d("----------setRepeating")
                alarmMgr!!.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    (1000 * 60 * minute).toLong(),
                    pendingIntent
                )
            } else {
                PreyLogger.d("----------setInexactRepeating")
                alarmMgr!!.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    (1000 * 60 * minute).toLong(),
                    pendingIntent
                )
            }
            PreyLogger.d("----------start report [$minute] ReportScheduled")
        } catch (e: Exception) {
            PreyLogger.e("----------Error ReportScheduled :${e.message}",e)
        }
    }

    /**
     * Resets the report scheduling process.
     *
     * This method cancels any currently scheduled alarms and logs a message
     * indicating that the report has been shut down.
     */
    fun reset() {
        if (alarmMgr != null) {
            try {
                var minute = PreyConfig.getInstance(context).getIntervalReport()!!.toInt()
                PreyLogger.d("_________________shutdown report [$minute] alarmIntent")
                minute = 0
            } catch (e: Exception) {
                PreyLogger.e("----------Error ReportScheduled ::${e.message}",e)
            }
        }
    }

    companion object {
        private var instance: ReportScheduled? = null
        fun getInstance(context: Context): ReportScheduled {
            return instance ?: ReportScheduled(context).also { instance = it }
        }
    }
}