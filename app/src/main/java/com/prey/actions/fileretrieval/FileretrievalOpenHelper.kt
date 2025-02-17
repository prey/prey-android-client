/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.prey.PreyLogger

/**
 * A SQLiteOpenHelper class responsible for managing the file retrieval database.
 */
class FileretrievalOpenHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Creates the file retrieval table when the database is created.
     *
     * @param db The SQLiteDatabase object.
     */
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(FILERETRIEVAL_TABLE_CREATE)
        } catch (e: Exception) {
            PreyLogger.e("Error creating table: ${e.message}", e)
        }
    }

    /**
     * Upgrades the database by dropping the existing table and recreating it.
     *
     * @param db The SQLiteDatabase object.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS ${FILERETRIEVAL_TABLE_NAME}")
        } catch (e: Exception) {
            PreyLogger.e("Erase error table: ${e.message}", e)
        }
        onCreate(db)
    }

    /**
     * Inserts a new file retrieval entry into the database.
     *
     * @param dto The FileretrievalDto object containing the file retrieval data.
     */
    fun insertFileretrieval(dto: FileretrievalDto) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_FILEID, dto.getFileId())
        values.put(COLUMN_PATH, dto.getPath())
        values.put(COLUMN_SIZE, dto.getSize())
        values.put(COLUMN_STATUS, dto.getStatus())
        PreyLogger.d("___db insert:${dto}")
        database.insert(FILERETRIEVAL_TABLE_NAME, null, values)
        database.close()
    }

    /**
     * Updates an existing file retrieval entry in the database.
     *
     * @param dto The FileretrievalDto object containing the updated file retrieval data.
     */
    fun updateFileretrieval(dto: FileretrievalDto) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PATH, dto.getPath())
        values.put(COLUMN_SIZE, dto.getSize())
        values.put(COLUMN_STATUS, dto.getStatus())
        val selection = "$COLUMN_FILEID = ?"
        val selectionArgs = arrayOf(dto.getFileId())
        PreyLogger.d("___db update:$dto")
        database.update(FILERETRIEVAL_TABLE_NAME, values, selection, selectionArgs)
        database.close()
    }

    /**
     * Deletes a file retrieval entry from the database by ID.
     *
     * @param id The ID of the file retrieval entry to delete.
     */
    fun deleteFileretrieval(id: String) {
        val database = this.writableDatabase
        val deleteQuery =
            "DELETE FROM  " + FILERETRIEVAL_TABLE_NAME + " where " + COLUMN_FILEID + "='" + id + "'"
        PreyLogger.d("query:$deleteQuery")
        database.execSQL(deleteQuery)
        database.close()
    }

    /**
     * Deletes all file retrieval entries from the database.
     */
    fun deleteAllFileretrieval() {
        val database = this.writableDatabase
        val deleteQuery = "DELETE FROM ${FILERETRIEVAL_TABLE_NAME}"
        PreyLogger.d("query:$deleteQuery")
        database.execSQL(deleteQuery)
        database.close()
    }

    /**
     * Retrieves all file retrieval entries from the database.
     *
     * @return A list of FileretrievalDto objects containing the file retrieval data.
     */
    val allFileretrieval: List<FileretrievalDto>
        get() {
            var cursor: Cursor? = null
            val list: MutableList<FileretrievalDto> = ArrayList()
            try {
                val selectQuery = "SELECT  * FROM ${FILERETRIEVAL_TABLE_NAME}"
                val database = this.readableDatabase
                cursor = database.rawQuery(selectQuery, null)
                if (cursor.moveToFirst()) {
                    do {
                        val dto = FileretrievalDto()
                        dto.setFileId(cursor.getString(0))
                        dto.setPath(cursor.getString(1))
                        dto.setSize(cursor.getLong(2))
                        dto.setStatus(cursor.getInt(3))
                        list.add(dto)
                    } while (cursor.moveToNext())
                }
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            } finally {
                if (cursor != null) {
                    try {
                        cursor.close()
                    } catch (e1: Exception) {
                        PreyLogger.e("Error:${e1.message}", e1)
                    }
                }
            }
            return list
        }

    /**
     * Retrieves a file retrieval entry from the database by ID.
     *
     * @param id The ID of the file retrieval entry to be retrieved.
     * @return The file retrieval data, or null if not found.
     */
    fun getFileretrieval(id: String): FileretrievalDto? {
        var cursor: Cursor? = null
        var dto: FileretrievalDto? = null
        try {
            val database = this.readableDatabase
            val selectQuery =
                "SELECT * FROM ${FILERETRIEVAL_TABLE_NAME} where ${COLUMN_FILEID}='${id}'"
            cursor = database.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    dto = FileretrievalDto()
                    dto.setFileId(cursor.getString(0))
                    dto.setPath(cursor.getString(1))
                    dto.setSize(cursor.getLong(2))
                    dto.setStatus(cursor.getInt(3))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        } finally {
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (e1: Exception) {
                    PreyLogger.e("Error:${e1.message}", e1)
                }
            }
        }
        return dto
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "Fileretrieval.db"
        const val FILERETRIEVAL_TABLE_NAME: String = "fileretrieval"
        const val COLUMN_FILEID: String = "_file_id"
        const val COLUMN_PATH: String = "_path"
        const val COLUMN_SIZE: String = "_size"
        const val COLUMN_STATUS: String = "_status"
        const val FILERETRIEVAL_TABLE_CREATE =
            "CREATE TABLE ${FILERETRIEVAL_TABLE_NAME} (${COLUMN_FILEID} TEXT PRIMARY KEY, ${COLUMN_PATH} TEXT,${COLUMN_SIZE} REAL,${COLUMN_STATUS} INTEGER);"
    }
}