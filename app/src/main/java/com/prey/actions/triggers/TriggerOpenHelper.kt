/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.prey.PreyLogger

/**
 * TriggerOpenHelper is a SQLiteOpenHelper that provides methods for creating, upgrading, and interacting with the trigger database.
 */
class TriggerOpenHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Called when the database is created for the first time.
     * Creates the trigger table with the specified columns.
     */
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(TRIGGER_TABLE_CREATE)
        } catch (e: Exception) {
            PreyLogger.e("Error creating table: ${e.message}", e)
        }
    }

    /**
     * Called when the database needs to be upgraded.
     * Drops the existing trigger table and recreates it.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS ${TRIGGER_TABLE_NAME}")
        } catch (e: Exception) {
            PreyLogger.e("Erase error table: ${e.message}", e)
        }
        onCreate(db)
    }

    /**
     * Inserts a new trigger into the database.
     * @param trigger The trigger to insert.
     */
    fun insertTrigger(trigger: TriggerDto) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ID, trigger.getId())
        values.put(COLUMN_NAME, trigger.getName())
        values.put(COLUMN_EVENTS, trigger.getEvents())
        values.put(COLUMN_ACTIONS, trigger.getActions())
        PreyLogger.d("___db insert:$trigger")
        database.insert(TRIGGER_TABLE_NAME, null, values)
        database.close()
    }

    /**
     * Updates an existing trigger in the database.
     * @param trigger The trigger to update.
     */
    fun updateTrigger(trigger: TriggerDto) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, trigger.getName())
        values.put(COLUMN_EVENTS, trigger.getEvents())
        values.put(COLUMN_ACTIONS, trigger.getActions())
        val selection = "${COLUMN_ID} = ?"
        val selectionArgs = arrayOf("${trigger.getId()}")
        PreyLogger.d("___db update:$trigger")
        database.update(TRIGGER_TABLE_NAME, values, selection, selectionArgs)
        database.close()
    }

    /**
     * Deletes a trigger from the database by its ID.
     *
     * @param id The ID of the trigger to delete.
     */
    fun deleteTrigger(id: String) {
        val database = this.writableDatabase
        val deleteQuery =
            "DELETE FROM  ${TRIGGER_TABLE_NAME} where ${COLUMN_ID}='${id}'"
        database.execSQL(deleteQuery)
        database.close()
    }

    /**
     * Deletes all triggers from the database.
     */
    fun deleteAllTrigger() {
        val database = this.writableDatabase
        val deleteQuery = "DELETE FROM $TRIGGER_TABLE_NAME"
        database.execSQL(deleteQuery)
        database.close()
    }

    /**
     * Retrieves all triggers from the database.
     *
     * @return A list of TriggerDto objects representing all triggers in the database.
     */
    val allTriggers: List<TriggerDto>
        get() {
            var cursor: Cursor? = null
            val list: MutableList<TriggerDto> = ArrayList()
            try {
                val selectQuery = "SELECT  * FROM ${TRIGGER_TABLE_NAME}"
                val database = this.readableDatabase
                cursor = database.rawQuery(selectQuery, null)
                if (cursor.moveToFirst()) {
                    do {
                        val trigger = TriggerDto()
                        trigger.setId(cursor.getString(0))
                        trigger.setName(cursor.getString(1))
                        trigger.setEvents(cursor.getString(2))
                        trigger.setActions(cursor.getString(3))
                        list.add(trigger)
                    } while (cursor.moveToNext())
                }
            } catch (e: Exception) {
                PreyLogger.e("error:${e.message}", e)
            } finally {
                if (cursor != null) {
                    try {
                        cursor.close()
                    } catch (e1: Exception) {
                        PreyLogger.e("error:${e1.message}", e1)
                    }
                }
            }
            return list
        }

    /**
     * Retrieves a trigger from the database by its ID.
     *
     * @param id The ID of the trigger to retrieve.
     * @return The TriggerDto object representing the trigger, or null if not found.
     */
    fun getTrigger(id: String): TriggerDto? {
        var cursor: Cursor? = null
        var trigger: TriggerDto? = null
        try {
            val database = this.readableDatabase
            val selectQuery =
                "SELECT * FROM ${TRIGGER_TABLE_NAME} where ${COLUMN_ID}='${id}'"
            cursor = database.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    trigger = TriggerDto()
                    trigger.setId(cursor.getString(0))
                    trigger.setName(cursor.getString(1))
                    trigger.setEvents(cursor.getString(2))
                    trigger.setActions(cursor.getString(3))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            PreyLogger.e("error:${e.message}", e)
        } finally {
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (e1: Exception) {
                    PreyLogger.e("error:${e1.message}", e1)
                }
            }
        }
        return trigger
    }

    companion object {
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "Trigger.db"
        const val TRIGGER_TABLE_NAME: String = "trigger"
        const val COLUMN_ID: String = "_id"
        const val COLUMN_NAME: String = "_name"
        const val COLUMN_EVENTS: String = "_events"
        const val COLUMN_ACTIONS: String = "_actions"
        const val TRIGGER_TABLE_CREATE =
            "CREATE TABLE ${TRIGGER_TABLE_NAME} (${COLUMN_ID} TEXT PRIMARY KEY,  ${COLUMN_NAME} TEXT, ${COLUMN_EVENTS} TEXT, ${COLUMN_ACTIONS} TEXT );"
    }

}