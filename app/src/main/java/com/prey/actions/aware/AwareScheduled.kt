/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import com.prey.PreyLogger

/**
 * A utility class for scheduling and canceling alarms.
 */
class AwareScheduled {

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    /**
     * Starts the alarm scheduling process.
     *
     * @param context The application context
     */
    fun start(context: Context) {
        try {
            val intent = Intent(context, AlarmAwareReceiver::class.java)
            pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            scheduleAlarm()
        } catch (e: Exception) {
            PreyLogger.e("----------Error AwareScheduled :${e.message}", e)
        }
    }

    /**
     * Schedules an alarm to trigger at a specified interval.
     */
    private fun scheduleAlarm() {
        val intervalMinutes = 15
        val triggerTime = System.currentTimeMillis()
        val alarmInterval = (1000 * 60 * intervalMinutes).toLong()
        scheduleAlarm(triggerTime, alarmInterval)
    }

    /**
     * Schedules an alarm to trigger at a specified time with a specified interval.
     *
     * @param triggerTime The time at which the alarm should first trigger
     * @param interval The interval in milliseconds between alarm triggers
     */
    private fun scheduleAlarm(triggerTime: Long, interval: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pendingIntent)
        } else {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pendingIntent)
        }
    }

    /**
     * Cancels any currently scheduled alarms.
     */
    fun cancel() {
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            PreyLogger.d("----------Error AwareScheduled :${e.message}")
        }
    }

    companion object {
        private var instance: AwareScheduled? = null
        fun getInstance(): AwareScheduled {
            return instance ?: AwareScheduled().also { instance = it }
        }
    }
}
