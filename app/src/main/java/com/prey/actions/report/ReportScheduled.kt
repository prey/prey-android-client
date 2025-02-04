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

class ReportScheduled private constructor(context: Context) {
    private var context: Context? = null
    private var alarmMgr: AlarmManager? = null


    init {
        this.context = context
    }

    fun run() {
        try {
            val minute = PreyConfig.getInstance(context!!).getIntervalReport()!!.toInt()
            PreyLogger.d("----------ReportScheduled start minute:$minute")
            val intent = Intent(context, AlarmReportReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmMgr = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
            PreyLogger.d("----------Error ReportScheduled :" + e.message)
        }
    }

    fun reset() {
        if (alarmMgr != null) {
            try {
                var minute = PreyConfig.getInstance(context!!).getIntervalReport()!!.toInt()
                PreyLogger.d("_________________shutdown report [$minute] alarmIntent")

                minute = 0
            } catch (e: Exception) {
                PreyLogger.d("----------Error ReportScheduled :" + e.message)
            }
        }
    }

    companion object {
        private var instance: ReportScheduled? = null

        @Synchronized
        fun getInstance(context: Context): ReportScheduled? {
            if (instance == null) {
                instance = ReportScheduled(context)
            }
            return instance
        }
    }
}