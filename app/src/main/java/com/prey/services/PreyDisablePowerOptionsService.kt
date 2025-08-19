/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.receivers.AlarmDisablePowerReceiver
import com.prey.receivers.PreyDisablePowerOptionsReceiver

/**
 * This service is responsible for disabling power options on the device.
 * It listens for the ACTION_CLOSE_SYSTEM_DIALOGS intent and registers a receiver to handle it.
 */
class PreyDisablePowerOptionsService : Service() {
    var mReceiver: BroadcastReceiver

    init {
        PreyLogger.d("PreyDisablePowerOptionsService  create ________")
        mReceiver = PreyDisablePowerOptionsReceiver()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Called when the service is started.
     * @param intent The Intent that started the service.
     * @param startId The ID of the service.
     */
    override fun onStart(intent: Intent, startId: Int) {
        val disablePowerOptions = PreyConfig.getInstance(
            applicationContext
        ).isDisablePowerOptions()
        PreyLogger.d("PreyDisablePowerOptionsService  onStart ________disablePowerOptions:$disablePowerOptions")
        if (disablePowerOptions) {
            val intentfilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            registerReceiver(mReceiver, intentfilter)
        }
    }

    /**
     * Called when the service is destroyed.
     */
    override fun onDestroy() {
        PreyLogger.d("PreyDisablePowerOptionsService  onDestroy__________")
        try {
            unregisterReceiver(mReceiver)
        } catch (e: IllegalArgumentException) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        val disablePowerOptions = PreyConfig.getInstance(
            applicationContext
        ).isDisablePowerOptions()
        if (disablePowerOptions) {
            schedule()
        }
        stopForeground(true)
    }

    /**
     * Called when the service is started with a specific command.
     * @param intent The Intent that started the service.
     * @param i The flags of the service.
     * @param j The start ID of the service.
     * @return The result of the service.
     */
    override fun onStartCommand(intent: Intent, i: Int, j: Int): Int {
        val disablePowerOptions = PreyConfig.getInstance(
            applicationContext
        ).isDisablePowerOptions()
        PreyLogger.d("PreyDisablePowerOptionsService  onStartCommand disablePowerOptions:$disablePowerOptions")
        if (disablePowerOptions) {
            val closeDialog = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            registerReceiver(mReceiver, closeDialog)
        }
        return START_STICKY
    }

    /**
     * Called when the service is removed from the recent tasks list.
     * @param rootIntent The Intent that started the service.
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        PreyLogger.d("Service stopped by Android, we program in 7 seconds")
        val disablePowerOptions = PreyConfig.getInstance(
            applicationContext
        ).isDisablePowerOptions()
        if (disablePowerOptions) {
            schedule()
        }
    }

    /**
     * Schedules the service to restart after 7 seconds.
     */
    private fun schedule() {
        PreyLogger.d("PreyDisablePowerOptionsService  schedule_________")
        val intent = Intent(applicationContext, AlarmDisablePowerReceiver::class.java)
        val alarmDisablePower =
            PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_MUTABLE)
        val alarmMgr = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            alarmMgr[AlarmManager.RTC, SystemClock.elapsedRealtime() + 7000L] =
                alarmDisablePower
            alarmMgr[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 7000L] =
                alarmDisablePower
        } else {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC, 7000L, alarmDisablePower)
        }
    }
}