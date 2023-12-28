/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2023 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.prey.PreyLogger;

import java.util.ArrayList;
import java.util.List;

public class LoggerOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PreyLogger.db";
    public static final String LOGGER_TABLE_NAME = "prey_logger";
    public static final String COLUMN_LOGGERID = "_logger_id";
    public static final String COLUMN_TYPE = "_type";
    public static final String COLUMN_TIME = "_time";
    public static final String COLUMN_TXT = "_txt";

    private static final String LOGGER_TABLE_CREATE =
            "CREATE TABLE ".concat(LOGGER_TABLE_NAME).concat(" (").concat(
                    COLUMN_LOGGERID).concat(" INTEGER PRIMARY KEY, ").concat(
                    COLUMN_TXT).concat(" TEXT,").concat(
                    COLUMN_TIME).concat(" TEXT,").concat(
                    COLUMN_TYPE).concat(" TEXT").concat(
                    ");");

    public LoggerOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(LOGGER_TABLE_CREATE);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error creating table:%s", e.getMessage()), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS ".concat(LOGGER_TABLE_NAME));
        } catch (Exception e) {
            PreyLogger.e(String.format("error drop table:%s", e.getMessage()), e);
        }
        onCreate(db);
    }

    public void insertLogger(LoggerDto dto) {
        PreyLogger.d("___db insertLogger");
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOGGERID, dto.getLoggerId());
        values.put(COLUMN_TXT, dto.getTxt());
        values.put(COLUMN_TYPE, dto.getType());
        values.put(COLUMN_TIME, dto.getTime());
        PreyLogger.d(String.format("___db insert:%s", dto.toString()));
        database.insert(LOGGER_TABLE_NAME, null, values);
        database.close();
    }

    public void updateLogger(LoggerDto dto) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TXT, dto.getTxt());
        values.put(COLUMN_TYPE, dto.getType());
        values.put(COLUMN_TIME, dto.getTime());
        String selection = COLUMN_LOGGERID + " = ?";
        String[] selectionArgs = {String.valueOf(dto.getLoggerId())};
        PreyLogger.d(String.format("___db update:%s", dto.toString()));
        database.update(LOGGER_TABLE_NAME, values, selection, selectionArgs);
        database.close();
    }

    public void deleteLogger(int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM ".concat(LOGGER_TABLE_NAME).concat(" where ").concat(COLUMN_LOGGERID).concat("=").concat(String.valueOf(id));
        PreyLogger.d(String.format("___db query:%s", deleteQuery));
        database.execSQL(deleteQuery);
        database.close();
    }

    public void deleteMinorsLogger(int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM ".concat(LOGGER_TABLE_NAME).concat(" where ").concat(COLUMN_LOGGERID).concat("<").concat(String.valueOf(id));
        PreyLogger.d(String.format("___db query:%s", deleteQuery));
        database.execSQL(deleteQuery);
        database.close();
    }

    public void deleteAllLogger() {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM ".concat(LOGGER_TABLE_NAME);
        PreyLogger.d(String.format("___db query:%s", deleteQuery));
        database.execSQL(deleteQuery);
        database.close();
    }

    public List<LoggerDto> getAllLogger() {
        Cursor cursor = null;
        List<LoggerDto> list = new ArrayList<LoggerDto>();
        try {
            String selectQuery = "SELECT  * FROM ".concat(LOGGER_TABLE_NAME);
            SQLiteDatabase database = this.getReadableDatabase();
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    LoggerDto dto = new LoggerDto();
                    dto.setLoggerId(cursor.getInt(0));
                    dto.setTxt(cursor.getString(1));
                    dto.setTime(cursor.getString(2));
                    dto.setType(cursor.getString(3));
                    list.add(dto);
                } while (cursor.moveToNext());
            }
            database.close();
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e1) {
                }
            }
        }
        return list;
    }

    public LoggerDto getLogger(int id) {
        Cursor cursor = null;
        LoggerDto dto = null;
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM ".concat(LOGGER_TABLE_NAME).concat(" where ").concat(COLUMN_LOGGERID).concat("='").concat(String.valueOf(id)).concat("'");
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    dto = new LoggerDto();
                    dto.setLoggerId(cursor.getInt(0));
                    dto.setTxt(cursor.getString(1));
                    dto.setTime(cursor.getString(2));
                    dto.setType(cursor.getString(3));
                } while (cursor.moveToNext());
            }
            database.close();
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e1) {
                    PreyLogger.e(String.format("Error:%s", e1.getMessage()), e1);
                }
            }
        }
        return dto;
    }

}