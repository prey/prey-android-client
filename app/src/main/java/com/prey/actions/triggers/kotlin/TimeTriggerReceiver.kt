/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers.kotlin

import android.content.Context
import android.content.Intent
import com.prey.kotlin.PreyLogger

class TimeTriggerReceiver : TriggerReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val triggerId = intent.extras?.getString("trigger_id")
            val dataSource = TriggerDataSource(context)
            val trigger = dataSource.getTrigger(triggerId ?: return)
            if (TriggerUtil.validateTrigger(trigger!!)) {
                executeActions(context, trigger.getActions())
            }
        } catch (e: Exception) {
            PreyLogger.e("Error processing trigger", e)
        }
    }
}