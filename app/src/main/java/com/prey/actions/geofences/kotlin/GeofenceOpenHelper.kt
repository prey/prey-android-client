/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences.kotlin

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.prey.kotlin.PreyLogger

class GeofenceOpenHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(GEOFENCE_TABLE_CREATE)
        } catch (e: Exception) {
            PreyLogger.e("Error creating table: " + e.message, e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + GEOFENCE_TABLE_NAME)
        } catch (e: Exception) {
            PreyLogger.e("Erase error table: " + e.message, e)
        }
        onCreate(db)
    }

    fun insertGeofence(geofence: GeofenceDto) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ID, geofence.getId())
        values.put(COLUMN_NAME, geofence.getName())
        values.put(COLUMN_LATITUDE, geofence.getLatitude())
        values.put(COLUMN_LONGITUDE, geofence.getLongitude())
        values.put(COLUMN_RADIUS, geofence.getRadius())
        values.put(COLUMN_EXPIRES, geofence.getExpires())
        PreyLogger.d("___db insert:$geofence")
        database.insert(GEOFENCE_TABLE_NAME, null, values)
        database.close()
    }

    fun updateGeofence(geofence: GeofenceDto) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, geofence.getName())
        values.put(COLUMN_LATITUDE, geofence.getLatitude())
        values.put(COLUMN_LONGITUDE, geofence.getLongitude())
        values.put(COLUMN_RADIUS, geofence.getRadius())
        values.put(COLUMN_EXPIRES, geofence.getExpires())
        val selection = COLUMN_ID + " = ?"
        val selectionArgs = arrayOf(geofence.getId())
        //PreyLogger.d("___db update:" + geofence.toString());
        database.update(GEOFENCE_TABLE_NAME, values, selection, selectionArgs)
        database.close()
    }

    fun updateGeofenceType(id: String, type: String?) {
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TYPE, type)
        val selection = COLUMN_ID + " = ?"
        val selectionArgs = arrayOf(id)
        //PreyLogger.d("___db update type:"+type+" id:" + id);
        database.update(GEOFENCE_TABLE_NAME, values, selection, selectionArgs)
        database.close()
    }


    fun deleteGeofence(id: String) {
        val database = this.writableDatabase
        val deleteQuery =
            "DELETE FROM  " + GEOFENCE_TABLE_NAME + " where " + COLUMN_ID + "='" + id + "'"
        //PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery)
        database.close()
    }

    fun deleteAllGeofence() {
        val database = this.writableDatabase
        val deleteQuery = "DELETE FROM  " + GEOFENCE_TABLE_NAME
        //PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery)
        database.close()
    }

    val allGeofences: List<GeofenceDto>
        get() {
            var cursor: Cursor? = null
            val list: MutableList<GeofenceDto> = ArrayList()
            try {
                val selectQuery = "SELECT  * FROM " + GEOFENCE_TABLE_NAME
                val database = this.readableDatabase
                cursor = database.rawQuery(selectQuery, null)
                if (cursor.moveToFirst()) {
                    do {
                        val geofence = GeofenceDto()
                        geofence.setId(cursor.getString(0))
                        geofence.setName(cursor.getString(1))
                        geofence.setLatitude(cursor.getDouble(2))
                        geofence.setLongitude(cursor.getDouble(3))
                        geofence.setRadius(cursor.getFloat(4))
                        geofence.setType(cursor.getString(5))
                        geofence.setExpires(cursor.getInt(6))
                        list.add(geofence)
                    } while (cursor.moveToNext())
                }
            } catch (e: Exception) {
                PreyLogger.e("error:" + e.message, e)
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

    fun getGeofence(id: String): GeofenceDto? {
        var cursor: Cursor? = null
        var geofence: GeofenceDto? = null
        try {
            val database = this.readableDatabase
            val selectQuery =
                "SELECT * FROM " + GEOFENCE_TABLE_NAME + " where " + COLUMN_ID + "='" + id + "'"
            cursor = database.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    geofence = GeofenceDto()
                    geofence.setId(cursor.getString(0))
                    geofence.setName(cursor.getString(1))
                    geofence.setLatitude(cursor.getDouble(2))
                    geofence.setLongitude(cursor.getDouble(3))
                    geofence.setRadius(cursor.getFloat(4))
                    geofence.setType(cursor.getString(5))
                    geofence.setExpires(cursor.getInt(6))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            PreyLogger.e("error:" + e.message, e)
        } finally {
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (e1: Exception) {
                }
            }
        }
        return geofence
    }

    companion object {
        private const val DATABASE_VERSION = 8
        private const val DATABASE_NAME = "Geofence.db"
        const val GEOFENCE_TABLE_NAME: String = "geofence"
        const val COLUMN_ID: String = "_id"
        const val COLUMN_NAME: String = "_name"
        const val COLUMN_LATITUDE: String = "_latitude"
        const val COLUMN_LONGITUDE: String = "_longitude"
        const val COLUMN_RADIUS: String = "_radius"
        const val COLUMN_TYPE: String = "_type"
        const val COLUMN_EXPIRES: String = "_expires"
        private const val GEOFENCE_TABLE_CREATE = "CREATE TABLE " + GEOFENCE_TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT," +
                COLUMN_LATITUDE + " REAL," +
                COLUMN_LONGITUDE + " REAL," +
                COLUMN_RADIUS + " REAL," +
                COLUMN_TYPE + " TEXT," +
                COLUMN_EXPIRES + " INTEGER" +
                ");"
    }
}