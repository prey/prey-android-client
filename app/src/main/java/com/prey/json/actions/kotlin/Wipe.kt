/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.content.Context
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.actions.wipe.kotlin.WipeThread
import com.prey.json.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import org.json.JSONObject

class Wipe {

    fun start(context: Context, list: MutableList<ActionResult>?, parameters: JSONObject?) {
        execute(context, list, parameters)
    }

    fun execute(context: Context, list: MutableList<ActionResult>?, parameters: JSONObject?) {
        var wipe = false
        var deleteSD = false
        try {
            var sd: String? = null
            if (parameters != null && parameters.has("parameter")) {
                sd = parameters.getString("parameter")
                PreyLogger.d(String.format("sd:%s", sd))
            }
            if (sd != null && "sd" == sd) {
                wipe = false
                deleteSD = true
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
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
        try {
            val factoryReset = UtilJson.getString(parameters, "factory_reset")
            PreyLogger.i(String.format("factoryReset:%s", factoryReset))
            if ("on" == factoryReset || "y" == factoryReset || "true" == factoryReset) {
                wipe = true
            }
            if ("off" == factoryReset || "n" == factoryReset || "false" == factoryReset) {
                wipe = false
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        try {
            val wipeSim = UtilJson.getString(parameters, "wipe_sim")
            PreyLogger.i(String.format("wipeSim:%s", wipeSim))
            if ("on" == wipeSim || "y" == wipeSim || "true" == wipeSim) {
                deleteSD = true
            }
            if ("off" == wipeSim || "n" == wipeSim || "false" == wipeSim) {
                deleteSD = false
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:", e.message), e)
        }
        PreyLogger.i(String.format("wipe:%b deleteSD%b:", wipe, deleteSD))
        WipeThread(context, wipe, deleteSD, messageId!!, jobId).start()
    }
}