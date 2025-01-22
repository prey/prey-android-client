/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers.kotlin

import android.content.Context
import com.prey.kotlin.PreyLogger

class TriggerDataSource(context: Context) {
    private val dbHelper = TriggerOpenHelper(context)

    fun createTrigger(trigger: TriggerDto) {
        try {
            dbHelper.insertTrigger(trigger)
        } catch (e: Exception) {
            try {
                dbHelper.updateTrigger(trigger)
            } catch (e1: Exception) {
                PreyLogger.e("Trigger error db update:" + e1.message, e1)
            }
        }
    }

    fun deleteTrigger(id: String) {
        dbHelper.deleteTrigger(id)
    }

    val allTriggers: List<TriggerDto>
        get() = dbHelper.allTriggers

    fun getTrigger(id: String): TriggerDto? {
        return dbHelper.getTrigger(id)
    }

    fun deleteAllTrigger() {
        dbHelper.deleteAllTrigger()
    }

    fun updateTrigger(trigger: TriggerDto) {
        dbHelper.updateTrigger(trigger)
    }
}