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
import com.prey.net.PreyWebServices

class AlertThread {
    private var notificationId: Int = 0
    private var context: Context? = null
    private var description: String? = null
    private var messageId: String? = null
    private var jobId: String? = null
    private var fullscreenNotification: Boolean = false

    fun run(
        context: Context,
        description: String,
        messageId: String?,
        jobId: String?,
        fullscreenNotification: Boolean
    ) {
        this.context = context
        this.description = description
        this.messageId = messageId
        this.jobId = jobId
        this.fullscreenNotification = fullscreenNotification
        notificationId = AlertConfig.getInstance(context)?.getNextNotificationId() ?: 0
        if (PreyUtils.isChromebook(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            runFullscreenAlert()
        } else if (fullscreenNotification) {
            runFullscreenAlert()
            runNotificationAlert()
        } else {
            runNotificationAlert()
        }
    }

    fun runNotificationAlert() {
        try {
            val channelId = "CHANNEL_ALERT_ID"
            val notificationId = notificationId
            val jobId = jobId
            val description = description
            val messageId = messageId
            val context = context

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(channelId, context!!)
            }

            val reason =
                if (jobId != null && jobId.isNotEmpty()) "{\"device_job_id\":\"$jobId\"}" else null

            val buttonIntent = Intent(context, AlertReceiver::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                action = notificationId.toString()
                putExtra("notificationId", notificationId)
                putExtra("messageId", messageId)
                putExtra("reason", reason)
            }

            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, buttonIntent, PendingIntent.FLAG_MUTABLE)

            val notificationManager =
                context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                createNotificationCompatBuilder(
                    notificationId,
                    channelId,
                    pendingIntent,
                    description!!,
                    context
                )
                    .also { notificationManager.notify(notificationId, it.build()) }
            } else {
                val contentViewSmall =
                    RemoteViews(context.packageName, R.layout.custom_notification_small)
                val contentViewBig = createBigContentView(description!!, context)

                contentViewBig.setOnClickPendingIntent(R.id.noti_button, pendingIntent)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notification = Notification.Builder(context, channelId)
                        .setSmallIcon(R.drawable.icon2)
                        .setCustomContentView(contentViewSmall)
                        .setCustomBigContentView(contentViewBig)
                        .setDeleteIntent(pendingIntent)
                        .setAutoCancel(true);
                    notificationManager.notify(notificationId, notification.build())
                } else {
                    val notification = NotificationCompat.Builder(context!!)
                        .setSmallIcon(R.drawable.icon2)
                        .setCustomContentView(contentViewSmall)
                        .setCustomBigContentView(contentViewBig)
                        .setDeleteIntent(pendingIntent)
                        .setAutoCancel(true);
                    notificationManager.notify(notificationId, notification.build())
                }
            }

            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                "processed",
                messageId,
                UtilJson.makeMapParam("start", "alert", "started", reason)
            )
        } catch (e: Exception) {
            PreyLogger.e("failed alert: " + e.message, e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context!!,
                messageId,
                UtilJson.makeMapParam("start", "alert", "failed", e.message)
            )
        }
    }

    private fun createNotificationCompatBuilder(
        notificationId: Int,
        channelId: String,
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, context: Context) {
        val channel =
            NotificationChannel(channelId, "prey_alert", NotificationManager.IMPORTANCE_HIGH)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        (context.getSystemService(NotificationManager::class.java) as NotificationManager).createNotificationChannel(
            channel
        )
    }

    private fun createBigContentView(description: String, context: Context): RemoteViews {
        var contentViewBig: RemoteViews? = null
        if (description.length <= 70) {
            contentViewBig = RemoteViews(context.packageName, R.layout.custom_notification1)
        } else {
            if (description.length <= 170) {
                contentViewBig = RemoteViews(context.packageName, R.layout.custom_notification2)
            } else {
                contentViewBig = RemoteViews(context.packageName, R.layout.custom_notification3)
            }
        }

        val notiBody = SpannableStringBuilder(description)
        notiBody.setSpan(
            CustomTypefaceSpan(context, "fonts/Regular/regular-book.otf"),
            0,
            notiBody.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        contentViewBig!!.setTextViewText(R.id.noti_body, notiBody)

        val maxlength = 45
        var descriptionSmall: String = description
        if (description.length > maxlength) {
            descriptionSmall = description.substring(0, maxlength) + ".."
        }
        val notiBodySmall = SpannableStringBuilder(descriptionSmall)
        notiBodySmall.setSpan(
            CustomTypefaceSpan(context, "fonts/Regular/regular-book.otf"),
            0,
            notiBodySmall.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )

        val closeAlert: String = context.getString(R.string.close_alert)
        val notiButton = SpannableStringBuilder(closeAlert)
        notiButton.setSpan(
            CustomTypefaceSpan(context, "fonts/Regular/regular-bold.otf"),
            0,
            notiButton.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        contentViewBig!!.setTextViewText(R.id.noti_button, notiButton)
        return contentViewBig
    }

    fun runFullscreenAlert() {
        try {
            val notificationPopupId = notificationId
            val alertTitle = "title"
            val alertDescription = description

            val intent = Intent(context, PopUpAlertActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("title_message", alertTitle)
                putExtra("alert_message", alertDescription)
                putExtra("description_message", alertDescription)
                putExtra("notificationId", notificationPopupId)
            }

            PreyConfig.getInstance(context!!).setNoficationPopupId(notificationPopupId)
            context!!.startActivity(intent)

            if (PreyUtils.isChromebook(context!!) || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Thread {
                    val reason = null
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                        context!!,
                        "processed",
                        messageId,
                        UtilJson.makeMapParam("start", "alert", "started", reason)
                    )
                    Thread.sleep(2000)
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                        context!!,
                        "processed",
                        messageId,
                        UtilJson.makeMapParam("start", "alert", "stopped", reason)
                    )
                }.start()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error running fullscreen alert", e)
        }
    }

    companion object {
        private var INSTANCE: AlertThread? = null
        fun getInstance(): AlertThread {
            if (INSTANCE == null) {
                INSTANCE = AlertThread()
            }
            return INSTANCE!!
        }
    }
}