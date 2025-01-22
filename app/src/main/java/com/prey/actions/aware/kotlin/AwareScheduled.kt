/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware.kotlin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.prey.kotlin.PreyLogger


class AwareScheduled (private val context: Context) {
    private var alarmMgr: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    fun run() {
        try {
            val minute = 15
            val intent = Intent(context, AlarmAwareReceiver::class.java)
            pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            alarmMgr = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                PreyLogger.d("----------setRepeating")
                alarmMgr!!.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    (1000 * 60 * minute).toLong(),
                    pendingIntent!!
                )
            } else {
                PreyLogger.d("----------setInexactRepeating")
                alarmMgr!!.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    (1000 * 60 * minute).toLong(),
                    pendingIntent!!
                )
            }
            PreyLogger.d(String.format("----------start aware [%s] AwareScheduled", minute))
        } catch (e: Exception) {
            PreyLogger.e(String.format("----------Error AwareScheduled :%s", e.message), e)
        }
    }

    fun reset() {
        if (alarmMgr != null) {
            try {
                alarmMgr!!.cancel(pendingIntent!!)
            } catch (e: Exception) {
                PreyLogger.d(String.format("----------Error AwareScheduled :%s", e.message))
            }
        }
    }

    companion object {
        private var instance: AwareScheduled? = null

        @Synchronized
        fun getInstance(context: Context): AwareScheduled? {
            if (instance == null) {
                instance = AwareScheduled(context)
            }
            return instance
        }
    }
}
