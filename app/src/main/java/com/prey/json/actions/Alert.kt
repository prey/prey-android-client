/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.actions.alert.AlertAction
import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import org.json.JSONObject


/**
 * Class representing an Alert, which can be started, canceled, or run.
 */
class Alert() {


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
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
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
        PreyLogger.d("Alert start")
        var alertMessage: String? = ""
        try {
            alertMessage = UtilJson.getStringValue(parameters, "alert_message")
        } catch (e: java.lang.Exception) {
            try {
                alertMessage = UtilJson.getStringValue(parameters, "message")
            } catch (e2: java.lang.Exception) {
                PreyLogger.e("Error:${e2.message}", e2)
            }
        }
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:${messageId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
            PreyLogger.d("jobId:${jobId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var isFullscreenNotification = false
        try {
            isFullscreenNotification =
                UtilJson.getBooleanValue(parameters, "fullscreen_notification")
            PreyLogger.d("fullscreen_notification:${isFullscreenNotification}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
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
            PreyLogger.d("startAlert:${alert}")
            // If the alert message is not null and not blank, start the alert thread.
            if (alert != null && alert.isNotBlank()) {
                AlertAction.getInstance()
                    .start(context, messageId, jobId, alert, isFullscreenNotification)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error, causa:${e.message}", e)
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "alert", "failed", e.message)
            )
        }
    }

}