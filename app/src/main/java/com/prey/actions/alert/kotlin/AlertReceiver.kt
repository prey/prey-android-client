/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert.kotlin

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.prey.activities.kotlin.PopUpAlertActivity
import com.prey.json.kotlin.UtilJson
import com.prey.net.kotlin.PreyWebServices


class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 0)
        val messageId = intent.getStringExtra("messageId")
        val reason = intent.getStringExtra("reason")

        val popupIntentAction = "${PopUpAlertActivity.POPUP_PREY}_$notificationId"
        context.sendBroadcast(Intent(popupIntentAction))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        Thread {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                "processed",
                messageId!!,
                UtilJson.makeMapParam("start", "alert", "stopped", reason)
            )
        }.start()
    }
}
