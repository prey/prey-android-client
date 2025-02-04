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

class AlarmReportReceiver : BroadcastReceiver() {
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
