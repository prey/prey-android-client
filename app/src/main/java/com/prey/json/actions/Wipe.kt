/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.PreyLogger
import com.prey.actions.wipe.WipeUtil
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.json.CommandTarget
import com.prey.json.UtilJson
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * A [CommandTarget] responsible for handling device wipe operations.
 *
 * This class processes remote commands to perform a factory reset on the device and/or
 * delete the contents of the SD card. It handles the "start" and "stop" commands
 * for the wipe action.
 *
 * The `start` command initiates the wipe process based on the provided options,
 * which can specify a full factory reset (`factory_reset`) or just wiping the SD card (`wipe_sim`).
 * The class notifies the backend service about the progress of the operation (started, stopped, error).
 *
 * The `stop` command is a placeholder that notifies the backend that the wipe command
 * has been "stopped", although the wipe process itself, once started, is generally irreversible.
 */
class Wipe : CommandTarget {

    override fun execute(context: Context, command: String, options: JSONObject): Any? {
        return when (command) {
            "start" -> start(context, options)
            "stop" -> stop(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Starts the device wipe process based on the provided options.
     *
     * This function is executed in a background coroutine. It first notifies the Prey web service
     * that the wipe process has started. It then parses the `options` to determine the scope of the wipe:
     * - `factory_reset`: If set to "on", "y", or "true", it triggers a full factory reset of the device.
     * - `wipe_sim`: If set to "on", "y", or "true", it triggers the deletion of the SD card's content.
     *   This is a legacy naming convention and does not affect the SIM card.
     *
     * The function handles both actions (SD card deletion and factory reset) and sends appropriate
     * notifications to the backend upon completion or if an error occurs. A factory reset is a destructive
     * and irreversible action.
     *
     * @param context The application context, used for accessing system services and sending notifications.
     * @param options A [JSONObject] containing the specific wipe commands, such as `factory_reset` or `wipe_sim`.
     */
    fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Wipe start options:${options}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PreyWebServicesKt.sendNotifyActions(
                    context,
                    UtilJson.makeJsonResponse("start", "wipe", "started")
                )
                var wipe = false
                var deleteSD = false
                var factoryReset = ""
                try {
                    factoryReset = options.getString("factory_reset")
                } catch (e: Exception) {
                }
                if ("on" == factoryReset || "y" == factoryReset || "true" == factoryReset) {
                    wipe = true
                }
                var wipeSim = ""
                try {
                    wipeSim = options.getString("wipe_sim")
                } catch (e: Exception) {
                }
                if ("on" == wipeSim || "y" == wipeSim || "true" == wipeSim) {
                    deleteSD = true
                }
                if (deleteSD) {
                    WipeUtil.deleteSD()
                    if (!wipe) {
                        val jsonData2 = JSONObject()
                        jsonData2.put("command", "start")
                        jsonData2.put("target", "wipe")
                        jsonData2.put("status", "stopped")
                        PreyWebServicesKt.sendNotifyActions(
                            context,
                            UtilJson.makeJsonResponse("start", "wipe", "stopped")
                        )
                    }
                }
                if (wipe) {
                    PreyLogger.d("Wiping the device!!")
                    val jsonData2 = JSONObject()
                    jsonData2.put("command", "start")
                    jsonData2.put("target", "wipe")
                    jsonData2.put("status", "stopped")
                    PreyWebServicesKt.sendNotifyActions(
                        context,
                        UtilJson.makeJsonResponse("start", "wipe", "stopped")
                    )
                    FroyoSupport.getInstance(context).wipe()
                }
            } catch (e: Exception) {
                PreyWebServicesKt.sendNotifyActions(
                    context,
                    UtilJson.makeJsonResponse("start", "wipe", "stopped", e.message)
                )
                PreyLogger.e("Error Wipe:${e.message}", e)
            }
        }
    }

    fun stop(context: Context, options: JSONObject) {
        PreyLogger.d("Wipe stop options:${options}")
        CoroutineScope(Dispatchers.IO).launch {
            PreyWebServicesKt.sendNotifyActions(
                context,
                UtilJson.makeJsonResponse("stop", "wipe", "stopped")
            )
        }
    }

}