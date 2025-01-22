/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval.kotlin

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.prey.kotlin.PreyLogger


class FileretrievalOpenHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(FILERETRIEVAL_TABLE_CREATE)
        } catch (e: Exception) {
            PreyLogger.e("Error creating table: " + e.message, e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + FILERETRIEVAL_TABLE_NAME)
        } catch (e: Exception) {
            PreyLogger.e("Erase error table: " + e.message, e)
        }
        onCreate(db)
    }

    fun insertFileretrieval(dto: FileretrievalDto) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_FILEID, dto.getFileId())
        values.put(COLUMN_PATH, dto.getPath())
        values.put(COLUMN_SIZE, dto.getSize())
        values.put(COLUMN_STATUS, dto.getStatus())
        PreyLogger.d("___db insert:$dto")
        database.insert(FILERETRIEVAL_TABLE_NAME, null, values)
        database.close()
    }

    fun updateFileretrieval(dto: FileretrievalDto) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PATH, dto.getPath())
        values.put(COLUMN_SIZE, dto.getSize())
        values.put(COLUMN_STATUS, dto.getStatus())
        val selection = COLUMN_FILEID + " = ?"
        val selectionArgs = arrayOf(dto.getFileId())
        PreyLogger.d("___db update:$dto")
        database.update(FILERETRIEVAL_TABLE_NAME, values, selection, selectionArgs)
        database.close()
    }

    fun deleteFileretrieval(id: String) {
        val database = this.writableDatabase
        val deleteQuery =
            "DELETE FROM  " + FILERETRIEVAL_TABLE_NAME + " where " + COLUMN_FILEID + "='" + id + "'"
        PreyLogger.d("query$deleteQuery")
        database.execSQL(deleteQuery)
        database.close()
    }

    fun deleteAllFileretrieval() {
        val database = this.writableDatabase
        val deleteQuery = "DELETE FROM  " + FILERETRIEVAL_TABLE_NAME
        PreyLogger.d("query$deleteQuery")
        database.execSQL(deleteQuery)
        database.close()
    }

    val allFileretrieval: List<FileretrievalDto>
        get() {
            var cursor: Cursor? = null
            val list: MutableList<FileretrievalDto> = ArrayList()
            try {
                val selectQuery = "SELECT  * FROM " + FILERETRIEVAL_TABLE_NAME
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
                PreyLogger.e("Error:" + e.message, e)
            } finally {
                if (cursor != null) {
                    try {
                        cursor.close()
                    } catch (e1: Exception) {
                    }
                }
            }
            return list
        }

    fun getFileretrieval(id: String): FileretrievalDto? {
        var cursor: Cursor? = null
        var dto: FileretrievalDto? = null
        try {
            val database = this.readableDatabase
            val selectQuery =
                "SELECT * FROM " + FILERETRIEVAL_TABLE_NAME + " where " + COLUMN_FILEID + "='" + id + "'"
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
            PreyLogger.e("Error:" + e.message, e)
        } finally {
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (e1: Exception) {
                }
            }
        }
        return dto
    }

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "Fileretrieval.db"
        const val FILERETRIEVAL_TABLE_NAME: String = "fileretrieval"
        const val COLUMN_FILEID: String = "_file_id"
        const val COLUMN_PATH: String = "_path"
        const val COLUMN_SIZE: String = "_size"
        const val COLUMN_STATUS: String = "_status"

        private const val FILERETRIEVAL_TABLE_CREATE =
            "CREATE TABLE " + FILERETRIEVAL_TABLE_NAME + " (" +
                    COLUMN_FILEID + " TEXT PRIMARY KEY, " +
                    COLUMN_PATH + " TEXT," +
                    COLUMN_SIZE + " REAL," +
                    COLUMN_STATUS + " INTEGER" +
                    ");"
    }
}