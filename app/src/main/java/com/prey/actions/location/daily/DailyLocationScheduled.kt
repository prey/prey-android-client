/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import com.prey.PreyLogger

/**
 * A utility class for scheduling daily location updates.
 */
class DailyLocationScheduled private constructor() {

    /**
     * Prepares an alarm to send the daily location.
     *
     * This method initializes the alarm manager and sets a repeating alarm to trigger at a specified interval.
     *
     * @param context The application context.
     */
    fun start(context: Context) {
        try {
            val minute = 15
            val intent = Intent(context, DailyLocationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                PreyLogger.d("DAILY----------LocationScheduled setRepeating")
                alarmMgr.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    (1000 * 60 * minute).toLong(),
                    pendingIntent
                )
            } else {
                PreyLogger.d("DAILY----------LocationScheduled setInexactRepeating")
                alarmMgr.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    (1000 * 60 * minute).toLong(),
                    pendingIntent
                )
            }
            PreyLogger.d("DAILY----------start [${minute}] LocationScheduled")
        } catch (e: Exception) {
            PreyLogger.e("DAILY----------Error LocationScheduled :${e.message}", e)
        }
    }

    companion object {
        private var instance: DailyLocationScheduled? = null
        fun getInstance(): DailyLocationScheduled {
            return instance ?: DailyLocationScheduled().also { instance = it }
        }
    }
}