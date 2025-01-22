/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers.kotlin

import android.content.Context
import android.content.Intent
import com.prey.events.factories.kotlin.EventFactory
import com.prey.kotlin.PreyLogger

class SimTriggerReceiver : TriggerReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val simState = intent.extras?.getString(EXTRA_SIM_STATE) ?: return
        if (simState == "ABSENT" && intent.action == EventFactory.SIM_STATE_CHANGED) {
            execute(context, "hardware_changed")
        }
    }

    companion object {
        const val EXTRA_SIM_STATE = "ss"
    }
}