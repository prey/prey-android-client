/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context

import com.prey.actions.alert.AlertThread
import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.net.PreyWebServices

import org.json.JSONObject

/**
 * Class representing an Alert, which can be started, canceled, or run.
 */
class Alert {

    /**
     * Cancels the alert with the given parameters.
     *
     * @param context The application context.
     * @param actionResults List of action results, or null if not available.
     * @param parameters JSONObject containing alert parameters, or null if not available.
     */
    fun cancel(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        // Extract the message ID from the parameters, if available.
        val messageId = parameters?.getString(PreyConfig.MESSAGE_ID)
        try {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                "processed",
                messageId!!,
                UtilJson.makeMapParam("cancel", "alert", "stopped", null)
            )
        } catch (e: Exception) {
            PreyLogger.d("Error:${e.message}")
        }
    }

    /**
     * Starts the alert with the given parameters.
     *
     * @param context The application context.
     * @param actionResults List of action results, or null if not available.
     * @param parameters JSONObject containing alert parameters, or null if not available.
     */
    fun start(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        // Extract the alert message, message ID, job ID, and full screen notification flag from the parameters.
        val alertMessage =
            parameters?.getString("alert_message") ?: parameters?.getString("message")
        val messageId = parameters?.getString(PreyConfig.MESSAGE_ID)
        val jobId = parameters?.getString(PreyConfig.JOB_ID)
        var isFullscreenNotification = parameters?.getBoolean("fullscreen_notification") ?: false
        if (!PreyPermission.areNotificationsEnabled(context)) {
            isFullscreenNotification = true
        }
        // Start the alert with the extracted parameters.
        startAlert(context, alertMessage, messageId, jobId, isFullscreenNotification)
    }

    /**
     * Starts the alert with the given parameters.
     *
     * @param context The application context.
     * @param alert The alert message, or null if not available.
     * @param messageId The message ID, or null if not available.
     * @param jobId The job ID, or null if not available.
     * @param isFullscreenNotification Flag indicating whether the alert should be displayed in full screen mode.
     */
    fun startAlert(
        context: Context,
        alert: String?,
        messageId: String?,
        jobId: String?,
        isFullscreenNotification: Boolean
    ) {
        try {
            // If the alert message is not null and not blank, start the alert thread.
            if (alert != null && alert.isNotBlank()) {
                AlertThread.getInstance()
                    .start(context, alert, messageId, jobId, isFullscreenNotification)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error, causa:${e.message}", e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "alert", "failed", e.message)
            )
        }
    }
}