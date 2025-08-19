package com.prey.actions.wipe

import android.content.Context
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.json.UtilJson

class WipeAction {

    fun start(
        context: Context,
        messageId: String?,
        jobId: String?,
        wipe: Boolean,
        deleteSD: Boolean,
    ) {
        var reason: String? = null
        if (jobId != null && "" != jobId) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        val preyConfig = PreyConfig.getInstance(context)
        preyConfig.getWebServices().sendNotifyActionResultPreyHttp(
            context, "processed",
            messageId, UtilJson.makeMapParam("start", "wipe", "started", reason)
        )
        try {
            if (deleteSD) {
                preyConfig.getWipe().deleteSD(context)
                if (!wipe) {
                    preyConfig.getWebServices().sendNotifyActionResultPreyHttp(
                        context, "processed",
                        messageId, UtilJson.makeMapParam("start", "wipe", "stopped", reason)
                    )
                }
            }
        } catch (e: Exception) {
            preyConfig.getWebServices().sendNotifyActionResultPreyHttp(
                context, "failed",
                messageId, UtilJson.makeMapParam("start", "wipe", "failed", e.message)
            )
            PreyLogger.e("Error: {e.message}", e)
        }
        try {
            if (wipe && preyConfig.isFroyoOrAbove()) {
                PreyLogger.d("Wiping the device!!")
                preyConfig.getWebServices().sendNotifyActionResultPreyHttp(
                    context, "processed",
                    messageId, UtilJson.makeMapParam("start", "wipe", "stopped", reason)
                )
                preyConfig.getWipe().wipeData(context)
            }
        } catch (e: Exception) {
            preyConfig.getWebServices().sendNotifyActionResultPreyHttp(
                context, "failed",
                messageId, UtilJson.makeMapParam("start", "wipe", "failed", e.message)
            )
            PreyLogger.e("Error: {e.message}", e)
        }
    }

}