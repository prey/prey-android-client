/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.actions.alert.AlertThread
import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.net.PreyWebServices
import org.json.JSONObject

class Alert   {
    fun run(
        ctx: Context,
        list: List<ActionResult>,
        parameters: JSONObject
    ): HttpDataService? {
        return null
    }


    /**
     * Method cancel
     *
     * @param ctx
     * @param list
     * @param parameters
     */
    fun cancel(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject?) {
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
        } catch (e: Exception) {
            PreyLogger.d(String.format("Error:%s", e.message))
        }
        try {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                "processed",
                messageId!!,
                UtilJson.makeMapParam("cancel", "alert", "stopped", null)
            )
        } catch (e: Exception) {
            PreyLogger.d(String.format("Error:%s", e.message))
        }
    }

    fun start(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject?) {
        var alert: String? = ""
        try {
            alert = UtilJson.getString(parameters, "alert_message")
        } catch (e: Exception) {
            try {
                alert = UtilJson.getString(parameters, "message")
            } catch (e2: Exception) {
                PreyLogger.e(String.format("Error:%s", e2.message), e2)
            }
        }
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d(String.format("messageId:%s", messageId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID)
            PreyLogger.d(String.format("jobId:%s", jobId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var fullscreen_notification = false
        try {
            fullscreen_notification = UtilJson.getBoolean(parameters, "fullscreen_notification")
            PreyLogger.d(String.format("fullscreen_notification:%s", fullscreen_notification))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        if (!PreyPermission.areNotificationsEnabled(ctx)) {
            fullscreen_notification = true
        }
        startAlert(ctx, alert!!, messageId, jobId, fullscreen_notification)
    }

    fun startAlert(
        ctx: Context,
        alert: String,
        messageId: String?,
        jobId: String?,
        fullscreen_notification: Boolean
    ) {
        try {
            if (alert != null && "" != alert) {
                AlertThread.getInstance().run (ctx, alert, messageId, jobId, fullscreen_notification)
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error, causa:%s", e.message), e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("start", "alert", "failed", e.message)
            )
        }
    }
}