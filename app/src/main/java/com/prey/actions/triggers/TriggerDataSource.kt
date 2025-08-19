/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.Context

import com.prey.PreyLogger

/**
 * TriggerDataSource is a data access object (DAO) that provides a layer of abstraction
 * between the application's business logic and the trigger data stored in the database.
 *
 * @param context The application context.
 */
class TriggerDataSource(context: Context) {
    private val dbHelper = TriggerOpenHelper(context)

    /**
     * Creates a new trigger in the database.
     *
     * If the trigger already exists, it will be updated instead.
     *
     * @param trigger The trigger to create.
     */
    fun createTrigger(trigger: TriggerDto) {
        try {
            dbHelper.insertTrigger(trigger)
        } catch (e: Exception) {
            try {
                dbHelper.updateTrigger(trigger)
            } catch (e1: Exception) {
                PreyLogger.e("Trigger error db update:${e1.message}", e1)
            }
        }
    }

    /**
     * Deletes a trigger from the database by its ID.
     *
     * @param id The ID of the trigger to delete.
     */
    fun deleteTrigger(id: String) {
        dbHelper.deleteTrigger(id)
    }

    /**
     * Retrieves a list of all triggers in the database.
     *
     * @return A list of TriggerDto objects representing all triggers in the database.
     */
    val allTriggers: List<TriggerDto>
        get() = dbHelper.allTriggers

    /**
     * Retrieves a trigger from the database by its ID.
     *
     * @param id The ID of the trigger to retrieve.
     * @return The TriggerDto object representing the trigger, or null if not found.
     */
    fun getTrigger(id: String): TriggerDto? {
        return dbHelper.getTrigger(id)
    }

    /**
     * Deletes all triggers from the database.
     */
    fun deleteAllTrigger() {
        dbHelper.deleteAllTrigger()
    }

    /**
     * Updates an existing trigger in the database.
     *
     * @param trigger The trigger to update.
     */
    fun updateTrigger(trigger: TriggerDto) {
        dbHelper.updateTrigger(trigger)
    }

}