/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

import com.prey.R
import com.prey.activities.PopUpAlertActivity
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Class responsible for managing alert threads.
 */
class AlertAction {

    private var messageId: String? = null
    private var jobId: String? = null
    private var description: String? = null
    private var fullscreenNotification: Boolean = false

    /**
     * Starts the alert.
     *
     * @param context Context in which the alert is being displayed
     * @param alertMessageId Message ID associated with the alert (optional)
     * @param alertJobId Job ID associated with the alert (optional)
     * @param alertDescription Description of the alert
     * @param isFullscreenNotification Flag indicating whether the alert should be displayed in full screen mode
     */
    fun start(
        context: Context,
        alertMessageId: String? = null,
        alertJobId: String? = null,
        alertDescription: String,
        isFullscreenNotification: Boolean
    ) {
        this.messageId = alertMessageId
        this.jobId = alertJobId
        this.description = alertDescription
        this.fullscreenNotification = isFullscreenNotification
        // Get the next available notification ID
        val notificationId = PreyConfig.getInstance(context).getNextNotificationId() ?: 0
        // Determine which type of alert to display based on the device and full screen flag
        when {
            // If the device is a Chromebook or the Android version is less than N, display a full screen alert
            PreyUtils.isChromebook(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.N -> runFullscreenAlert(
                context,
                notificationId
            )
            // If the full screen flag is set, display both a full screen alert and a notification alert
            isFullscreenNotification -> {
                runFullscreenAlert(context, notificationId)
                runNotificationAlert(context, notificationId)
            }
            // Otherwise, display only a notification alert
            else -> runNotificationAlert(context, notificationId)
        }
    }

    /**
     * Runs a notification alert.
     *
     * This function is responsible for creating and displaying a notification alert to the user.
     * It handles the creation of the notification intent, pending intent, and notification manager,
     * and also sends a notification result to the server.
     */
    private fun runNotificationAlert(context: Context, notificationId: Int) {
        PreyLogger.d("runNotificationAlert")
        try {
            // Get the notification ID, job ID, description, message ID, and context
            val jobId = this.jobId
            val description = this.description
            val messageId = this.messageId
            // Create a notification channel if the Android version is O or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(context)
            }
            // Create a reason string from the job ID, if available
            val reason = jobId?.let { "{\"device_job_id\":\"$it\"}" }
            // Create an intent for the AlertReceiver
            val intent = Intent(context, AlertReceiver::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = notificationId.toString()
                putExtra("notificationId", notificationId)
                putExtra("messageId", messageId)
                putExtra("reason", reason)
            }
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Create the notification based on the Android version
            val notification: Notification = when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> createNotificationCompatBuilder(
                    pendingIntent,
                    description!!,
                    context
                ).build()
                // Otherwise, use the standard builder
                else -> buildNotification(pendingIntent, description!!, context)
            }
            // Notify the notification manager with the notification
            notificationManager.notify(notificationId, notification)
            // Send a notification result to the server
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                "processed",
                messageId,
                UtilJson.makeMapParam("start", "alert", "started", reason)
            )
        } catch (e: Exception) {
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                messageId,
                UtilJson.makeMapParam("start", "alert", "failed", e.message)
            )
        }
    }

    /**
     * Builds a notification with a custom small and big content view.
     *
     * @param pendingIntent The pending intent to be triggered when the notification is clicked.
     * @param description The description of the notification.
     * @param context The context in which the notification is being built.
     * @return The built notification.
     */
    private fun buildNotification(
        pendingIntent: PendingIntent,
        description: String,
        context: Context
    ): Notification {
        val smallContentView = RemoteViews(context.packageName, R.layout.custom_notification_small)
        val bigContentView = createBigContentView(description, context)
        bigContentView.setOnClickPendingIntent(R.id.noti_button, pendingIntent)
        // Build the notification based on the Android version
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon2)
                .setCustomContentView(smallContentView)
                .setCustomBigContentView(bigContentView)
                .setDeleteIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        } else {
            // For Android versions below O, use the NotificationCompat.Builder
            NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon2)
                .setCustomContentView(smallContentView)
                .setCustomBigContentView(bigContentView)
                .setDeleteIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        }
    }

    /**
     * Creates a notification builder with a custom style and actions.
     *

     * @param pendingIntent The pending intent to be triggered when the notification is clicked.
     * @param description The description of the notification.
     * @param context The context in which the notification is being built.
     * @return A NotificationCompat.Builder object.
     */
    private fun createNotificationCompatBuilder(
        pendingIntent: PendingIntent,
        description: String,
        context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.icon2)
            .setContentTitle(context.getString(R.string.title_alert))
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .addAction(R.drawable.xx2, context.getString(R.string.close_alert), pendingIntent)
            .setDeleteIntent(pendingIntent)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    /**
     * Creates a notification channel with high importance.
     *
     * @param context The context in which the notification channel is being created.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val notificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(ALERT_CHANNEL_ID, "prey_alert", importance)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * Creates a notification channel with high importance.
     *
     * @param context The context in which the notification channel is being created.
     */
    private fun createBigContentView(description: String, context: Context): RemoteViews {
        val contentViewBig: RemoteViews = when {
            description.length <= 70 -> RemoteViews(
                context.packageName,
                R.layout.custom_notification1
            )

            description.length <= 170 -> RemoteViews(
                context.packageName,
                R.layout.custom_notification2
            )

            else -> RemoteViews(context.packageName, R.layout.custom_notification3)
        }
        val notificationBody = SpannableStringBuilder(description)
        notificationBody.setSpan(
            CustomTypefaceSpan(context, "fonts/Regular/regular-book.otf"),
            0,
            notificationBody.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        contentViewBig.setTextViewText(R.id.noti_body, notificationBody)
        val truncatedDescription = if (description.length > 45) {
            "${description.substring(0, 45)}.."
        } else {
            description
        }
        val truncatedNotificationBody = SpannableStringBuilder(truncatedDescription)
        truncatedNotificationBody.setSpan(
            CustomTypefaceSpan(context, "fonts/Regular/regular-book.otf"),
            0,
            truncatedNotificationBody.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        val closeButtonLabel = context.getString(R.string.close_alert)
        val closeButton = SpannableStringBuilder(closeButtonLabel)
        closeButton.setSpan(
            CustomTypefaceSpan(context, "fonts/Regular/regular-bold.otf"),
            0,
            closeButton.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        contentViewBig.setTextViewText(R.id.noti_button, closeButton)
        return contentViewBig
    }

    /**
     * Runs a full-screen alert.
     *
     * This function is responsible for creating and displaying a full-screen alert to the user.
     * It handles the creation of the alert intent, sets the notification ID, and starts the alert activity.
     * Additionally, it sends a notification result to the server if the device is a Chromebook or the Android version is less than N.
     */
    private fun runFullscreenAlert(context: Context, notificationId: Int) {
        PreyLogger.d("runFullscreenAlert")
        try {
            val alertTitleValue = "title"
            val alertDescriptionValue = description
            val popupIntent = Intent(context, PopUpAlertActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("title_message", alertTitleValue)
                putExtra("alert_message", alertDescriptionValue)
                putExtra("description_message", alertDescriptionValue)
                putExtra("notificationId", notificationId)
            }
            PreyConfig.getInstance(context).setNoficationPopupId(notificationId)
            context.startActivity(popupIntent)
            if (PreyUtils.isChromebook(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                CoroutineScope(Dispatchers.IO).launch {
                    PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                        context,
                        "processed",
                        messageId,
                        UtilJson.makeMapParam("start", "alert", "started", null)
                    )
                    Thread.sleep(2000)
                    PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                        context,
                        "processed",
                        messageId,
                        UtilJson.makeMapParam("start", "alert", "stopped", null)
                    )
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error running fullscreen alert", e)
        }
    }

    companion object {
        const val ALERT_CHANNEL_ID = "CHANNEL_ALERT_ID"

        private var instance: AlertAction? = null
        fun getInstance(): AlertAction {
            instance = instance ?: AlertAction()
            return instance!!
        }
    }

}