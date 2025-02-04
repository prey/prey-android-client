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
import com.prey.beta.services.PreyBetaRunnerService
import com.prey.exceptions.PreyException
import com.prey.json.parser.JSONParser
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import com.prey.net.UtilConnection
import org.json.JSONObject

class PreyBetaActionsRunner(private val ctx: Context, private val cmd: String?) : Runnable {
    private val messageId: String? = null

    override fun run() {
        execute()
    }

    fun execute() {
        if (PreyConfig.getInstance(ctx).isThisDeviceAlreadyRegisteredWithPrey(true)) {
            var connection = false
            try {
                var jsonObject: List<JSONObject>? = null
                connection = UtilConnection.getInstance().isInternetAvailable()
                if (connection) {
                    try {
                        jsonObject = if (cmd == null || "" == cmd) {
                            getInstructions(ctx, true)
                        } else {
                            getInstructionsNewThread(
                                ctx,
                                cmd, true
                            )
                        }
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
                PreyLogger.e("Error, because:" + e.message, e)
            }
            PreyLogger.d("Prey execution has finished!!")
        }
        ctx.stopService(Intent(ctx, PreyBetaRunnerService::class.java))
    }

    @Throws(PreyException::class)
    fun runInstructions(jsonObject: List<JSONObject>): List<HttpDataService>? {
        var listData: List<HttpDataService>? = null
        listData = ActionsController.getInstance().runActionJson(ctx, jsonObject)
        return listData
    }

    companion object {
        @Throws(PreyException::class)
        fun getInstructionsNewThread(ctx: Context, cmd: String?, close: Boolean): List<JSONObject>? {
            val jsonObject = JSONParser().getJSONFromTxt(
                ctx,
                "[$cmd]"
            )
            val context = ctx
            Thread {
                try {
                    PreyLogger.d("_________New Thread")
                    getInstructions(context, close)
                } catch (e: PreyException) {
                    PreyLogger.e("_________getInstructionsNewThread:" + e.message, e)
                }
            }.start()
            return jsonObject
        }

        @Throws(PreyException::class)
        private fun getInstructions(ctx: Context, close: Boolean): List<JSONObject>? {
            PreyLogger.d("______________________________")
            PreyLogger.d("_______getInstructions________")
            var jsonObject: List<JSONObject>? = null
            try {
                if (close) {
                    ctx.sendBroadcast(Intent(CheckPasswordHtmlActivity.CLOSE_PREY))
                }
                jsonObject = PreyWebServices.getInstance().getActionsJsonToPerform(ctx)
            } catch (e: PreyException) {
                PreyLogger.e("Exception getting device's xml instruction set", e)
                throw e
            }
            return jsonObject
        }
    }
}