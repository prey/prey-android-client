/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions

import android.content.Context

import com.prey.PreyConfig

/**
 * Controller class for Prey beta actions.
 */
class PreyBetaController {

    /**
     * Starts the Prey beta actions with a null command.
     *
     * @param context The application context.
     */
    fun startPrey(context: Context) {
        startPrey(context, null)
    }

    /**
     * Starts the Prey beta actions with a given command.
     *
     * @param context The application context.
     * @param cmd The command to execute (optional).
     */
    fun startPrey(context: Context, cmd: String?) {
        // Check if the device is already registered with Prey.
        if (PreyConfig.getInstance(context).isThisDeviceAlreadyRegisteredWithPrey()) {
            Thread {
                // Get the Prey beta actions runner instance and execute the actions.
                PreyBetaActionsRunner.getInstance(context).execute()
            }.start()
        }
    }

    companion object {
        private var instance: PreyBetaController? = null
        fun getInstance(): PreyBetaController =
            instance ?: PreyBetaController().also { instance = it }
    }

}