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
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
class Wipe : CommandTarget, BaseAction() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    companion object {
        private const val TARGET = "wipe"
        private const val OPT_FACTORY_RESET = "factory_reset"
        private const val OPT_WIPE_SIM = "wipe_sim"
    }

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_START -> scope.launch { start(context, options) }
            CMD_STOP -> scope.launch { stop(context, options) }
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
    suspend fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Wipe start options:${options}")
        try {
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STARTED)
            val isFactoryReset = options.optBooleanLegacy(OPT_FACTORY_RESET)
            val isWipeSdCard = options.optBooleanLegacy(OPT_WIPE_SIM)
            if (isWipeSdCard) {
                WipeUtil.deleteSD()
                if (!isFactoryReset) {
                    PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STOPPED)
                }
            }
            if (isFactoryReset) {
                PreyLogger.d("Wiping the device!!")
                PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STOPPED)
                FroyoSupport.getInstance(context).wipe()
            }
        } catch (e: Exception) {
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_FAILED, e.message)
            PreyLogger.e("Error Wipe:${e.message}", e)
        }
    }

    /**
     * Stops the wipe process and notifies the backend service.
     *
     * This function sends a notification to the Prey web service indicating that the
     * wipe command has reached a "stopped" state. Since a factory reset is usually
     * irreversible once initiated, this method primarily serves to synchronize the
     * action status with the web panel.
     *
     * @param context The application context used for web service communication.
     */
    suspend fun stop(context: Context, options: JSONObject) {
        PreyLogger.d("Wipe stop options:${options}")
        PreyWebServicesKt.notify(context, CMD_STOP, TARGET, STATUS_STOPPED)
    }

    /**
     * Extension to handle Prey's legacy logic where booleans
     * can come as "on", "and", "true", or actual booleans.
     */
    private fun JSONObject.optBooleanLegacy(key: String): Boolean {
        if (!has(key)) return false
        val value = optString(key).lowercase()
        return value == "on" || value == "y" || value == "true" || optBoolean(key, false)
    }

}