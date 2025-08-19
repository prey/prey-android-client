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

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.activities.PopUpAlertActivity
import com.prey.json.UtilJson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        var messageId: String? = ""
        try {
            messageId = intent.getStringExtra(MESSAGE_ID_EXTRA)
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var reason: String? = ""
        try {
            reason = intent.getStringExtra(REASON_EXTRA)
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        // Create a popup Intent action based on the notification ID
        val popupIntentAction = "${PopUpAlertActivity.POPUP_PREY}_${notificationId}"
        context.sendBroadcast(Intent(popupIntentAction))
        // Cancel the notification with the given ID
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
        // Send a notification result to the server in a separate thread
        CoroutineScope(Dispatchers.IO).launch {
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                ACTION_RESULT_PROCESSED,
                messageId,
                UtilJson.makeMapParam(START, ALERT, STOPPED, reason)
            )
        }
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