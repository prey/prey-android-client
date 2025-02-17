/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.prey.actions.report.ReportService
import com.prey.PreyLogger

/**
 * AlarmReportReceiver is a BroadcastReceiver that listens for alarm events and triggers the ReportService.
 */
class AlarmReportReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            PreyLogger.d("______________________________")
            PreyLogger.d("______________________________")
            PreyLogger.d("----------AlarmReportReceiver onReceive")
            object : Thread() {
                override fun run() {
                    ReportService().run(context)
                }
            }.start()
        } catch (e: Exception) {
            PreyLogger.e("_______AlarmReportReceiver error:" + e.message, e)
        }
    }
}
