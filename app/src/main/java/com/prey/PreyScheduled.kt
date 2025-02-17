/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import com.prey.receivers.AlarmScheduledReceiver

/**
 * A utility class for scheduling and canceling alarms.
 */
class PreyScheduled {

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    /**
     * Starts the alarm scheduling process.
     *
     * @param context The application context
     */
    fun start(context: Context) {
        val intent = Intent(context, AlarmScheduledReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleAlarm()
    }

    /**
     * Schedules an alarm to trigger at a specified interval.
     */
    private fun scheduleAlarm() {
        val intervalMinutes = 15
        val triggerTime = System.currentTimeMillis()
        val interval = (1000 * 60 * intervalMinutes).toLong()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pendingIntent)
        } else {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                interval,
                pendingIntent
            )
        }
    }

    /**
     * Cancels any currently scheduled alarms.
     */
    fun cancel() {
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            PreyLogger.d("----------Error PreyScheduled :${e.message}")
        }
    }

    companion object {
        private var instance: PreyScheduled? = null
        fun getInstance(): PreyScheduled =
            instance ?: PreyScheduled().also { instance = it }
    }
}