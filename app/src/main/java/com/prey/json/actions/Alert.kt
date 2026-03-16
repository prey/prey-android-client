/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.R
import com.prey.actions.alert.AlertConfig
import com.prey.actions.alert.AlertReceiver
import com.prey.actions.alert.CustomTypefaceSpan
import com.prey.activities.PopUpAlertActivity
import com.prey.json.CommandTarget
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
class Alert : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    companion object {
        private const val TARGET = "alert"
        const val CHANNEL_ALERT_ID = "CHANNEL_ALERT_ID"
    }

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_START -> scope.launch { start(context, options) }
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
    suspend fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Alert start options:${options}")
        val notificationId = AlertConfig.getAlertConfig(context).getNotificationId()
        val alertMessage = options.optString("alert_message", null)
        val messageId = options.optString(PreyConfig.MESSAGE_ID, null)
        val jobId = options.optString(PreyConfig.JOB_ID, null)
        val reason = jobId?.let { "{\"device_job_id\":\"$it\"}" }
        if (alertMessage == null)
            return
        try {
            showFullscreenAlert(context, alertMessage, notificationId)
            createNotification(context, alertMessage, notificationId, messageId, reason)
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STARTED, reason, messageId)
        } catch (e: Exception) {
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_FAILED, e.message, messageId)
        }
    }

    /**
     * Launches the full-screen alert activity.
     *
     * This method persists the notification ID in the configuration, prepares an intent
     * for [PopUpAlertActivity], and starts the activity with the necessary flags to
     * ensure it appears as a new task even when called from a background context.
     *
     * @param context The application context.
     * @param alertMessage The message to be displayed in the alert.
     * @param notificationId The unique identifier for the notification associated with this alert.
     * @throws Exception If there is an error while starting the activity.
     */
    @Throws(Exception::class)
    fun showFullscreenAlert(
        context: Context,
        alertMessage: String,
        notificationId: Int
    ) {
        PreyConfig.getPreyConfig(context).noficationPopupId = notificationId
        PreyLogger.d("Starting fullscreen alert: $notificationId")
        val popupIntent = Intent(context, PopUpAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            //Pass extras directly (we avoid creating a Bundle manually if it's not necessary)
            putExtra("title_message", "title")
            putExtra("description_message", alertMessage)
            putExtra(
                "alert_message",
                alertMessage
            )
            putExtra("notificationId", notificationId)
        }
        //Start the activity
        context.startActivity(popupIntent)
    }

    /**
     * Creates and displays a high-priority system notification for the security alert.
     *
     * This method handles the complete notification lifecycle:
     * 1. Validates POST_NOTIFICATIONS permissions for Android 13+.
     * 2. Initializes the notification channel for Android Oreo+.
     * 3. Configures a [PendingIntent] to handle dismissal via [AlertReceiver].
     * 4. Selects a dynamic layout based on the message length for modern Android versions (Marshmallow+).
     * 5. Falls back to standard [NotificationCompat.BigTextStyle] for older versions.
     * 6. Triggers the notification through the [NotificationManager].
     *
     * @param context The application context.
     * @param alertMessage The text message to display within the notification.
     * @param notificationId A unique integer ID used to identify the notification and its dismissal intent.
     * @param messageId The optional ID of the message from the server, used for tracking responses.
     * @param reason An optional JSON string containing metadata (like job ID) related to why the alert was triggered.
     */
    fun createNotification(
        context: Context,
        alertMessage: String,
        notificationId: Int,
        messageId: String?,
        reason: String? = null
    ) {
        //Permission Validation (Fixed for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                PreyLogger.d("Permission POST_NOTIFICATIONS not granted. Skipping notification.")
                return
            }
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //Create Channel (Oreo+ Only)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "prey_alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                description = context.getString(R.string.channel_security_alerts)
            }
            notificationManager.createNotificationChannel(channel)
        }
        //Configure the PendingIntent
        val buttonIntent = Intent(context, AlertReceiver::class.java).apply {
            action = "ACTION_DISMISS_$notificationId"
            putExtra("notificationId", notificationId)
            putExtra("messageId", messageId)
            reason?.let { putExtra("reason", it) }
        }
        //Flags: IMMUTABLE is preferable unless you need to modify the intent later
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val btPendingIntent =
            PendingIntent.getBroadcast(context, notificationId, buttonIntent, pendingFlags)
        //Base construction
        val builder = NotificationCompat.Builder(context, CHANNEL_ALERT_ID).apply {
            setSmallIcon(R.drawable.icon2)
            setDeleteIntent(btPendingIntent)
            setAutoCancel(true)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setCategory(NotificationCompat.CATEGORY_ALARM)
        }
        //Differentiation of Layouts (M+) vs Standard (Pre-M)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val layoutRes = when {
                alertMessage.length <= 70 -> R.layout.custom_notification1
                alertMessage.length <= 170 -> R.layout.custom_notification2
                else -> R.layout.custom_notification3
            }
            val contentViewBig = RemoteViews(context.packageName, layoutRes)
            val contentViewSmall =
                RemoteViews(context.packageName, R.layout.custom_notification_small)
            applyCustomFonts(context, contentViewBig, contentViewSmall, alertMessage)
            contentViewBig.setOnClickPendingIntent(R.id.noti_button, btPendingIntent)
            builder.setCustomContentView(contentViewSmall)
            builder.setCustomBigContentView(contentViewBig)
            //It maintains the system header style (app icon, time, etc.)
            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } else {
            builder.setContentTitle(context.getString(R.string.title_alert))
                .setContentText(alertMessage)
                .setStyle(NotificationCompat.BigTextStyle().bigText(alertMessage))
                .addAction(R.drawable.xx2, context.getString(R.string.close_alert), btPendingIntent)
                .setContentIntent(btPendingIntent)
        }
        //Launch and persist
        notificationManager.notify(notificationId, builder.build())
        PreyConfig.getPreyConfig(context).isNextAlert = true
    }

    /**
     * Applies custom fonts and formatting to the notification layouts using [Spannable
     */
    private fun applyCustomFonts(
        context: Context,
        bigView: RemoteViews,
        smallView: RemoteViews,
        message: String
    ) {
        val regularBold = "fonts/Regular/regular-bold.otf"
        val regularBook = "fonts/Regular/regular-book.otf"
        //Main body
        val spannableMessage = SpannableStringBuilder(message).apply {
            setSpan(
                CustomTypefaceSpan(context, regularBook),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        bigView.setTextViewText(R.id.noti_body, spannableMessage)
        //Text for small view (truncated)
        val smallText = if (message.length > 45) "${message.substring(0, 45)}.." else message
        val spannableSmall = SpannableStringBuilder(smallText).apply {
            setSpan(
                CustomTypefaceSpan(context, regularBook),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        smallView.setTextViewText(R.id.noti_body, spannableSmall)
        //Lock button
        val closeText = context.getString(R.string.close_alert)
        val spannableButton = SpannableStringBuilder(closeText).apply {
            setSpan(
                CustomTypefaceSpan(context, regularBold),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        bigView.setTextViewText(R.id.noti_button, spannableButton)
    }

}