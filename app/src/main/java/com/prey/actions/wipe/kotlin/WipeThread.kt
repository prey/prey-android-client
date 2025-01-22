/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.wipe.kotlin

import android.content.Context
import com.prey.backwardcompatibility.kotlin.FroyoSupport
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices

class WipeThread(
    private val ctx: Context,
    private val wipe: Boolean,
    private val deleteSD: Boolean,
    private val messageId: String,
    private val jobId: String?
) :
    Thread() {
    override fun run() {
        var reason: String? = null
        if (jobId != null && "" != jobId) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        val preyConfig = PreyConfig.getInstance(ctx)
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
            ctx,
            UtilJson.makeMapParam("start", "wipe", "started", reason)
        )
        try {
            if (deleteSD) {
                WipeUtil.deleteSD()
                if (!wipe) {
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                        ctx,
                        UtilJson.makeMapParam("start", "wipe", "stopped", reason)
                    )
                }
            }
        } catch (e: Exception) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("start", "wipe", "failed", e.message)
            )
            PreyLogger.e("Error Wipe:" + e.message, e)
        }
        try {
            if (wipe && preyConfig.isFroyoOrAbove()) {
                PreyLogger.d("Wiping the device!!")
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    ctx,
                    UtilJson.makeMapParam("start", "wipe", "stopped", reason)
                )
                FroyoSupport.getInstance(ctx)!!.wipe()
            }
        } catch (e: Exception) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("start", "wipe", "failed", e.message)
            )
            PreyLogger.e("Error Wipe:" + e.message, e)
        }
    }
}