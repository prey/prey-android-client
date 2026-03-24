/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prey.PreyLogger
import com.prey.activities.PopUpAlertActivity
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * [BroadcastReceiver] responsible for handling alert-related actions, typically triggered from notifications.
 *
 * This receiver performs several key tasks:
 */
class AlertReceiver : BroadcastReceiver() {

    //We use a Scope linked to the process life cycle or a globally controlled one
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Handles the broadcast received when an alert action is triggered.
     *
     * This method extracts notification metadata, notifies the [PopUpAlertActivity] to update the UI,
     * cancels the active notification, and reports the event status to Prey web services
     * asynchronously using [goAsync] to ensure completion.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received containing extras such as "notificationId", "messageId", and "reason".
     */
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val notificationId: Int = intent.getIntExtra("notificationId", 0)
        val messageId: String? = intent.getStringExtra("messageId")
        val reason: String? = intent.getStringExtra("reason")
        PreyLogger.d("AlertReceiver notificationId:${notificationId}")
        val popupAction = "${PopUpAlertActivity.POPUP_PREY}_${notificationId}"
        PreyLogger.d("AlertReceiver popup intent:${popupAction}")
        //Notify PopUp activity
        context.sendBroadcast(Intent(popupAction).apply {
            //We ensure that only our app receives this internal broadcast, if possible.
            setPackage(context.packageName)
        })
        //Cancel the notification securely
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(notificationId)
        CoroutineScope(Dispatchers.IO).launch {
            PreyWebServicesKt.notify(context, "start", "alert", "stopped", reason, messageId, "processed")
        }
        //Optimized Background Work
        //Note: If the process dies, the coroutine might be canceled.
        //For critical tasks, consider WorkManager.
        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                PreyWebServicesKt.notify(
                    context, "start", "alert", "stopped",
                    reason, messageId, "processed"
                )
            } catch (e: Exception) {
                PreyLogger.e("Error notifying web services", e)
            } finally {
                pendingResult.finish() //It indicates that the broadcast has finished its asynchronous work.
            }
        }
    }

}