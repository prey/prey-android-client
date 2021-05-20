/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.prey.PreyLogger;

import java.util.ArrayList;
import java.util.List;

public class TriggerOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "Trigger.db";
    public static final String TRIGGER_TABLE_NAME = "trigger";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "_name";
    public static final String COLUMN_EVENTS = "_events";
    public static final String COLUMN_ACTIONS = "_actions";
    private static final String TRIGGER_TABLE_CREATE =
            "CREATE TABLE " + TRIGGER_TABLE_NAME + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_EVENTS + " TEXT," +
                    COLUMN_ACTIONS + " TEXT" +
                    ");";

    public TriggerOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(TRIGGER_TABLE_CREATE);
        } catch (Exception e) {
            PreyLogger.e("Error creating table: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TRIGGER_TABLE_NAME);
        } catch (Exception e) {
            PreyLogger.e("Erase error table: " + e.getMessage(), e);
        }
        onCreate(db);
    }

    public void insertTrigger(TriggerDto trigger) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, trigger.getId());
        values.put(COLUMN_NAME, trigger.getName());
        values.put(COLUMN_EVENTS, trigger.getEvents());
        values.put(COLUMN_ACTIONS, trigger.getActions());
        PreyLogger.d("___db insert:" + trigger.toString());
        database.insert(TRIGGER_TABLE_NAME, null, values);
        database.close();
    }

    public void updateTrigger(TriggerDto trigger) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, trigger.getName());
        values.put(COLUMN_EVENTS, trigger.getEvents());
        values.put(COLUMN_ACTIONS, trigger.getActions());
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {"" + trigger.getId()};
        PreyLogger.d("___db update:" + trigger.toString());
        database.update(TRIGGER_TABLE_NAME, values, selection, selectionArgs);
        database.close();
    }

    public void deleteTrigger(String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + TRIGGER_TABLE_NAME + " where " + COLUMN_ID + "='" + id + "'";
        //PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public void deleteAllTrigger() {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + TRIGGER_TABLE_NAME;
        //PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public List<TriggerDto> getAllTriggers() {
        Cursor cursor = null;
        List<TriggerDto> list = new ArrayList<TriggerDto>();
        try {
            String selectQuery = "SELECT  * FROM " + TRIGGER_TABLE_NAME;
            SQLiteDatabase database = this.getReadableDatabase();
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    TriggerDto trigger = new TriggerDto();
                    trigger.setId(cursor.getString(0));
                    trigger.setName(cursor.getString(1));
                    trigger.setEvents(cursor.getString(2));
                    trigger.setActions(cursor.getString(3));
                    list.add(trigger);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            PreyLogger.e("error:" + e.getMessage(), e);
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

    public TriggerDto getTrigger(String id) {
        Cursor cursor = null;
        TriggerDto trigger = null;
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TRIGGER_TABLE_NAME + " where " + COLUMN_ID + "='" + id + "'";
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    trigger = new TriggerDto();
                    trigger.setId(cursor.getString(0));
                    trigger.setName(cursor.getString(1));
                    trigger.setEvents(cursor.getString(2));
                    trigger.setActions(cursor.getString(3));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            PreyLogger.e("error:" + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e1) {
                }
            }
        }
        return trigger;
    }

}