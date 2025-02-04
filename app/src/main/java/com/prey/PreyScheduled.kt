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
import com.prey.receivers.AlarmScheduledReceiver
import java.util.Calendar

class PreyScheduled private constructor(context: Context) {
    private var context: Context? = null
    private var alarmMgr: AlarmManager? = null
    private var alarmIntent: PendingIntent? = null

    init {
        this.context = context
    }

    fun run(minute: Int) {
        val ctx = context
        if (minute > 0) {
            reset()
            val intent = Intent(context, AlarmScheduledReceiver::class.java)
            alarmIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            alarmMgr = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MINUTE, minute)

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                alarmMgr!!.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    (1000 * 60 * minute).toLong(),
                    alarmIntent!!
                )
            } else {
                alarmMgr!!.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    (1000 * 60 * minute).toLong(),
                    alarmIntent!!
                )
            }
            PreyLogger.d("SCHEDULE_____________start scheduled [$minute] alarmIntent")
        }
    }

    fun reset() {
        if (alarmMgr != null) {
            PreyLogger.d("_________________shutdown scheduled alarmIntent")
            alarmMgr!!.cancel(alarmIntent!!)
        }
    }

    companion object {
        private var instance: PreyScheduled? = null

        @Synchronized
        fun getInstance(context: Context): PreyScheduled? {
            if (instance == null) {
                instance = PreyScheduled(context)
            }
            return instance
        }
    }
}