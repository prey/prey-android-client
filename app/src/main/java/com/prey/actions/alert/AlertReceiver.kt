/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.prey.activities.PopUpAlertActivity
import com.prey.json.UtilJson
import com.prey.net.PreyWebServices

/**
 * A BroadcastReceiver that handles alert notifications.
 */
class AlertReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the Intent is being received.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Extract the notification ID, message ID, and reason from the Intent extras
        val notificationId = intent.getIntExtra(NOTIFICATION_ID_EXTRA, 0)
        val messageId = intent.getStringExtra(MESSAGE_ID_EXTRA)
        val reason = intent.getStringExtra(REASON_EXTRA)
        // Create a popup Intent action based on the notification ID
        val popupIntentAction = "${PopUpAlertActivity.POPUP_PREY}_${notificationId}"
        context.sendBroadcast(Intent(popupIntentAction))
        // Cancel the notification with the given ID
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
        // Send a notification result to the server in a separate thread
        Thread {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                ACTION_RESULT_PROCESSED,
                messageId!!,
                UtilJson.makeMapParam(START, ALERT, STOPPED, reason)
            )
        }.start()
    }

    companion object {
        private const val NOTIFICATION_ID_EXTRA = "notificationId"
        private const val MESSAGE_ID_EXTRA = "messageId"
        private const val REASON_EXTRA = "reason"
        private const val START = "start"
        private const val ALERT = "alert"
        private const val STOPPED = "stopped"
        private const val ACTION_RESULT_PROCESSED = "processed"
    }
}