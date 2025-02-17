/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.wipe

import android.content.Context

import com.prey.backwardcompatibility.FroyoSupport
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices

/**
 * A thread responsible for wiping the device data.
 *
 * @param context The application context.
 * @param wipe Whether to wipe the device data.
 * @param deleteSD Whether to delete the SD card data.
 * @param messageId The message ID associated with the wipe action.
 * @param jobId The job ID associated with the wipe action.
 */
class WipeThread(
    private val context: Context,
    private val wipe: Boolean,
    private val deleteSD: Boolean,
    private val messageId: String,
    private val jobId: String?
) :
    Thread() {

    /**
     * Runs the wipe thread.
     */
    override fun run() {
        var reason: String? = null
        if (jobId != null && "" != jobId) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        val preyConfig = PreyConfig.getInstance(context)
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
            context,
            UtilJson.makeMapParam("start", "wipe", "started", reason)
        )
        try {
            if (deleteSD) {
                WipeUtil.deleteSD()
                if (!wipe) {
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                        context,
                        UtilJson.makeMapParam("start", "wipe", "stopped", reason)
                    )
                }
            }
        } catch (e: Exception) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "wipe", "failed", e.message)
            )
            PreyLogger.e("Error Wipe:" + e.message, e)
        }
        try {
            if (wipe && preyConfig.isFroyoOrAbove()) {
                PreyLogger.d("Wiping the device!!")
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    context,
                    UtilJson.makeMapParam("start", "wipe", "stopped", reason)
                )
                FroyoSupport.getInstance(context)!!.wipe()
            }
        } catch (e: Exception) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "wipe", "failed", e.message)
            )
            PreyLogger.e("Error Wipe:" + e.message, e)
        }
    }
}