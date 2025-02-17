/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.Context
import android.content.Intent
import com.prey.events.factories.EventFactory

/**
 * A BroadcastReceiver that listens for SIM state changes and triggers the execution of actions when the SIM is absent.
 */
class SimTriggerReceiver : TriggerReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
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