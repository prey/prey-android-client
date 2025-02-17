/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions

import android.content.Context
import android.content.Intent

import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionsController
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.exceptions.PreyException
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import com.prey.net.UtilConnection

import org.json.JSONObject

/**
 * This class represents a runner for Prey beta actions. It is responsible for executing actions based on instructions received from the Prey web services.
 */
class PreyBetaActionsRunner(private val context: Context) : Runnable {
    private val messageId: String? = null

    /**
     * Runs the execute method when the runner is started.
     */
    override fun run() {
        execute()
    }

    /**
     * Executes the Prey beta actions. This method checks if the device is registered with Prey, retrieves instructions from the Prey web services, and runs the instructions if available.
     */
    fun execute() {
        if (PreyConfig.getInstance(context).isThisDeviceAlreadyRegisteredWithPrey(true)) {
            try {
                var jsonObject: List<JSONObject>? = null
                val internetConnectionAvailable = UtilConnection.getInstance().isInternetAvailable()
                if (internetConnectionAvailable) {
                    try {
                        jsonObject = getInstructions(context, true)
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                    if (jsonObject == null || jsonObject.size == 0) {
                        PreyLogger.d("nothing")
                    } else {
                        PreyLogger.d("runInstructions")
                        runInstructions(jsonObject)
                    }
                }
            } catch (e: Exception) {
                PreyLogger.e("Error, because:${e.message}", e)
            }
            PreyLogger.d("Prey execution has finished!!")
        }

    }

    /**
     * Runs the instructions received from the Prey web services. This method uses the ActionsController to execute the actions.
     *
     * @param jsonObject The JSON object containing the instructions
     * @return A list of HttpDataService objects representing the executed actions
     * @throws PreyException If an error occurs during action execution
     */
    @Throws(PreyException::class)
    fun runInstructions(jsonObject: List<JSONObject>): List<HttpDataService>? {
        return ActionsController.getInstance().runActionJson(context, jsonObject)
    }

    /**
     * Retrieves instructions from the Prey web services in a new thread. This method is used to avoid blocking the main thread.
     *
     * @param context The context in which to retrieve instructions
     * @param close Whether to close the Prey activity after retrieving instructions
     * @throws PreyException If an error occurs during instruction retrieval
     */
    @Throws(PreyException::class)
    fun getInstructionsNewThread(context: Context, close: Boolean) {
        Thread {
            try {
                PreyLogger.d("_________New Thread")
                val instructions = getInstructions(context, close)
                if (instructions != null) {
                    runInstructions(instructions)
                }
            } catch (e: PreyException) {
                PreyLogger.e("_________getInstructionsNewThread:${e.message}", e)
            }
        }.start()

    }

    /**
     * Retrieves instructions from the Prey web services. This method checks if the Prey activity should be closed after retrieving instructions.
     *
     * @param context The context in which to retrieve instructions
     * @param close Whether to close the Prey activity after retrieving instructions
     * @return A JSON object containing the instructions
     * @throws PreyException If an error occurs during instruction retrieval
     */
    @Throws(PreyException::class)
    fun getInstructions(context: Context, close: Boolean): List<JSONObject>? {
        PreyLogger.d("______________________________")
        PreyLogger.d("_______getInstructions________")
        var jsonObject: List<JSONObject>? = null
        try {
            if (close) {
                context.sendBroadcast(Intent(CheckPasswordHtmlActivity.CLOSE_PREY))
            }
            jsonObject = PreyWebServices.getInstance().getActionsJsonToPerform(context)
        } catch (e: PreyException) {
            PreyLogger.e("Exception getting device's xml instruction set", e)
            throw e
        }
        return jsonObject
    }

    companion object {
        private var instance: PreyBetaActionsRunner? = null
        fun getInstance(context: Context): PreyBetaActionsRunner =
            instance ?: PreyBetaActionsRunner(context).also { instance = it }
    }
}