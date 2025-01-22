/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions.kotlin

import android.content.Context
import android.content.Intent
import com.prey.beta.services.kotlin.PreyBetaRunnerService
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

class PreyBetaController {

    fun startPrey(ctx: Context) {
        startPrey(ctx, null)
    }

    fun startPrey(ctx: Context, cmd: String?) {
        val config = PreyConfig.getInstance(ctx)
        PreyLogger.d("startPrey:" + config.isThisDeviceAlreadyRegisteredWithPrey())
        if (config.isThisDeviceAlreadyRegisteredWithPrey()) {
            val context = ctx
            Thread {
                try {
                    context.stopService(Intent(context, PreyBetaRunnerService::class.java))
                    val intentStart = Intent(context, PreyBetaRunnerService::class.java)
                    if (cmd != null) {
                        intentStart.putExtra("cmd", cmd)
                    }
                    context.startService(intentStart)
                } catch (e: Exception) {
                    PreyLogger.e("error:" + e.message, e)
                }
            }.start()
        }
    }

    fun stopPrey(ctx: Context) {
        ctx.stopService(Intent(ctx, PreyBetaRunnerService::class.java))
    }

    companion object {
        private var INSTANCE: PreyBetaController? = null
        fun getInstance(): PreyBetaController {
            if (PreyBetaController.INSTANCE == null) {
                PreyBetaController.INSTANCE = PreyBetaController()
            }
            return PreyBetaController.INSTANCE!!
        }
    }

}