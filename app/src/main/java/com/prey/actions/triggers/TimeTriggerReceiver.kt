/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.Context
import android.content.Intent

import com.prey.PreyLogger

/**
 * A BroadcastReceiver that receives time-based trigger events.
 */
class TimeTriggerReceiver : TriggerReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val triggerId = intent.extras?.getString("trigger_id")
            val dataSource = TriggerDataSource(context)
            val trigger = dataSource.getTrigger(triggerId ?: return)
            if (TriggerUtil.validateTrigger(trigger!!)) {
                executeActions(context, trigger.getActions())
            }
        } catch (e: Exception) {
            PreyLogger.e("Error processing trigger:${e.message}", e)
        }
    }
}