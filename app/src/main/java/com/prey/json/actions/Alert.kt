/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.R
import com.prey.actions.alert.AlertConfig
import com.prey.actions.alert.AlertReceiver
import com.prey.actions.alert.CustomTypefaceSpan
import com.prey.activities.PopUpAlertActivity
import com.prey.json.CommandTarget
import com.prey.json.UtilJson
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Manages the "Alert" action triggered remotely.
 *
 * This class is responsible for handling commands related to the alert feature.
 * When a "start" command is received, it triggers a high-priority alert on the device.
 * The alert consists of two parts:
 * 1. A full-screen activity (`PopUpAlertActivity`) that overlays other apps.
 * 2. A persistent, high-priority system notification.
 *
 * The alert message is extracted from the command options. Both the full-screen display
 * and the notification will show this message and provide an option for the user to dismiss it.
 * Dismissing the alert notifies the Prey server that the action has been acknowledged.
 */
class Alert : CommandTarget {

    override fun execute(context: Context, command: String, options: JSONObject): Any? {
        return when (command) {
            "start" -> start(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Starts the alert action. This involves parsing the alert message and other
     * details from the incoming options, then displaying both a fullscreen activity
     * and a system notification to the user.
     *
     * It extracts the alert message, message ID, and job ID from the `options` JSON object.
     * It then notifies the Prey servers that the alert has started and proceeds to
     * launch a `PopUpAlertActivity` for an immediate, fullscreen alert and also posts
     * a persistent notification in the system tray.
     *
     * @param context The application context.
     * @param options A [JSONObject] containing the data for the alert, expecting keys like
     *                "alert_message" (or "message"), "message_id", and "job_id".
     */
    fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Alert start options:${options}")
        val notificationId = AlertConfig.getAlertConfig(context).getNotificationId()
        var alertMessage: String? = ""
        try {
            alertMessage = options.getString("alert_message")
        } catch (e: java.lang.Exception) {
            try {
                alertMessage = options.getString("message")
            } catch (e2: java.lang.Exception) {
                PreyLogger.e("Error:${e2.message}", e2)
            }
        }
        var messageId: String = ""
        try {
            if (options.has(PreyConfig.MESSAGE_ID)) {
                messageId = options.getString(PreyConfig.MESSAGE_ID)
                PreyLogger.d(String.format("messageId:%s", messageId))
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var reason: String? = null
        try {
            if (options.has(PreyConfig.JOB_ID)) {
                val jobId = options.getString(PreyConfig.JOB_ID)
                reason = "{\"device_job_id\":\"${jobId}\"}"
                PreyLogger.d("jobId:${jobId}")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        if (alertMessage == null)
            return
        CoroutineScope(Dispatchers.IO).launch {
            PreyWebServicesKt.sendNotifyActions(
                context,
                UtilJson.makeJsonResponse("start", "alert", "started", reason),
                messageId
            )
            fullscreen(context, alertMessage, notificationId, messageId)
            notification(context, alertMessage, notificationId, messageId, reason)
        }
    }

    /**
     * Displays a full-screen alert message.
     *
     * This function launches a `PopUpAlertActivity` to show a modal, full-screen alert
     * that overlays other applications. It's used to deliver high-priority messages to the user.
     * The activity is started with flags to ensure it's a new task, clearing any previous
     * instances.
     *
     * @param context The application context, used to start the activity and access system services.
     * @param alertMessage The main message content to be displayed in the full-screen alert.
     * @param notificationId The unique ID for the associated notification, used to manage it later.
     * @param messageId The unique identifier for this specific message command, used for tracking.
     */
    fun fullscreen(context: Context, alertMessage: String, notificationId: Int, messageId: String) {
        try {
            PreyConfig.getPreyConfig(context).setNoficationPopupId(notificationId)
            PreyLogger.d("started alert")
            val title = "title"
            val bundle = Bundle()
            bundle.putString("title_message", title)
            bundle.putString("alert_message", alertMessage)
            val popup = Intent(context, PopUpAlertActivity::class.java)
            popup.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            popup.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            popup.putExtras(bundle)
            popup.putExtra("description_message", alertMessage)
            popup.putExtra("notificationId", notificationId)
            context.startActivity(popup)
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error PopUpAlert:${e.message}", e)
        }
    }

    /**
     * Creates and displays a system notification for an alert.
     *
     * This function is responsible for building and showing a notification to the user.
     * It handles different Android versions, creating a notification channel for Oreo and above.
     * For newer devices (Marshmallow and up), it uses custom layouts (`RemoteViews`) for both
     * the collapsed and expanded states of the notification, allowing for custom fonts and styling.
     * The expanded layout is chosen based on the length of the `alertMessage`.
     * For older devices, it falls back to a standard `NotificationCompat.Builder` with a `BigTextStyle`.
     *
     * The notification includes a "close" action that, when tapped, broadcasts an intent to
     * `AlertReceiver` to handle the dismissal logic, such as sending a confirmation to the server.
     *
     * @param context The application context, used to access system services and resources.
     * @param alertMessage The primary message content to be displayed in the notification.
     * @param notificationId The unique integer identifier for this notification.
     * @param messageId The unique string identifier for the message that triggered this alert.
     * @param reason An optional JSON string containing additional data, like a job ID,
     *               to be sent back when the notification is actioned.
     */
    fun notification(
        context: Context,
        alertMessage: String,
        notificationId: Int,
        messageId: String,
        reason: String?
    ) {
        try {
            val NOTIFICATION_CHANNEL_ID = "10002"
            PreyLogger.d("started alert")
            PreyLogger.d("alertMessage:${alertMessage}")
            val CHANNEL_ID = "CHANNEL_ALERT_ID"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name: CharSequence = "prey_alert"
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_HIGH
                )
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC)
                val notificationManager: NotificationManager =
                    context.getSystemService<NotificationManager?>(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
            PreyLogger.d("notificationId:${notificationId}")
            val buttonIntent = Intent(context, AlertReceiver::class.java)
            buttonIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            buttonIntent.setAction("$notificationId")
            buttonIntent.putExtra("notificationId", notificationId)
            buttonIntent.putExtra("messageId", messageId)
            if (reason != null) {
                buttonIntent.putExtra("reason", reason)
            }
            val btPendingIntent =
                PendingIntent.getBroadcast(context, 0, buttonIntent, PendingIntent.FLAG_MUTABLE)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                val builder: NotificationCompat.Builder = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.icon2)
                    .setContentTitle(context.getString(R.string.title_alert))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(alertMessage))
                    .addAction(
                        R.drawable.xx2,
                        context.getString(R.string.close_alert),
                        btPendingIntent
                    )
                    .setDeleteIntent(btPendingIntent)
                    .setContentIntent(btPendingIntent)
                    .setAutoCancel(true)
                notificationManager.notify(notificationId, builder.build())
            } else {
                var contentViewBig: RemoteViews? = null
                if (alertMessage.length <= 70) {
                    PreyLogger.d("custom_notification1 length:${alertMessage.length}")
                    contentViewBig =
                        RemoteViews(context.getPackageName(), R.layout.custom_notification1)
                } else {
                    if (alertMessage.length <= 170) {
                        PreyLogger.d("custom_notification2 length:${alertMessage.length}")
                        contentViewBig =
                            RemoteViews(context.getPackageName(), R.layout.custom_notification2)
                    } else {
                        PreyLogger.d("custom_notification3 length:${alertMessage.length}")
                        contentViewBig =
                            RemoteViews(context.getPackageName(), R.layout.custom_notification3)
                    }
                }
                val contentViewSmall: RemoteViews =
                    RemoteViews(context.getPackageName(), R.layout.custom_notification_small)
                contentViewBig!!.setOnClickPendingIntent(R.id.noti_button, btPendingIntent)
                val regularBold = "fonts/Regular/regular-bold.otf"
                val regularBook = "fonts/Regular/regular-book.otf"
                val notiBody = SpannableStringBuilder(alertMessage)
                notiBody.setSpan(
                    CustomTypefaceSpan(context, regularBook),
                    0,
                    notiBody.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                contentViewBig.setTextViewText(R.id.noti_body, notiBody)
                val maxlength = 45
                var descriptionSmall: String? = alertMessage
                if (alertMessage.length > maxlength) {
                    descriptionSmall = "${alertMessage.substring(0, maxlength)}.."
                }
                val notiBodySmall = SpannableStringBuilder(descriptionSmall)
                notiBodySmall.setSpan(
                    CustomTypefaceSpan(context, regularBook),
                    0,
                    notiBodySmall.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                contentViewSmall.setTextViewText(R.id.noti_body, notiBodySmall)
                val close_alert: String? = context.getString(R.string.close_alert)
                PreyLogger.d("close_alert:${close_alert}")
                val notiButton = SpannableStringBuilder(close_alert)
                notiButton.setSpan(
                    CustomTypefaceSpan(context, regularBold),
                    0,
                    notiButton.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                contentViewBig.setTextViewText(R.id.noti_button, notiButton)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notification: Notification.Builder =
                        Notification.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.icon2)
                            .setCustomContentView(contentViewSmall)
                            .setCustomBigContentView(contentViewBig)
                            .setDeleteIntent(btPendingIntent)
                            .setAutoCancel(true)
                    notificationManager.notify(notificationId, notification.build())
                } else {
                    val builder: NotificationCompat.Builder = NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon2)
                        .setCustomContentView(contentViewSmall)
                        .setCustomBigContentView(contentViewBig)
                        .setDeleteIntent(btPendingIntent)
                        .setAutoCancel(true)
                    notificationManager.notify(notificationId, builder.build())
                }
            }
            PreyConfig.getPreyConfig(context).setNextAlert(true)
        } catch (e: Exception) {
            PreyLogger.e("failed alert:${e.message}", e)
        }
    }

}