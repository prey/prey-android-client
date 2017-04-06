/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.offline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.prey.PreyLogger;

import java.util.ArrayList;
import java.util.List;

public class OfflineOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "Offline.db";

    public static final String OFFLINE_TABLE_NAME = "offline";

    public static final String COLUMN_OFFLINE_ID = "_offline_id";
    public static final String COLUMN_URL = "_url";
    public static final String COLUMN_TYPE = "_type";
    public static final String COLUMN_PARAMETERS = "_parameters";
    public static final String COLUMN_REQUEST_METHOD = "_requestMethod";
    public static final String COLUMN_CONTENT_TYPE = "_contentType";
    public static final String COLUMN_AUTHORIZATION = "_authorization";
    public static final String COLUMN_STATUS = "_status";
    public static final String COLUMN_CORRELATION_ID = "_correlationId";
    public static final String COLUMN_FILES = "_files";

    private static final String OFFLINE_TABLE_CREATE =
            "CREATE TABLE " + OFFLINE_TABLE_NAME + " (" +
                    COLUMN_OFFLINE_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_URL + " TEXT," +
                    COLUMN_TYPE + " TEXT," +
                    COLUMN_PARAMETERS + " TEXT," +
                    COLUMN_REQUEST_METHOD + " TEXT," +
                    COLUMN_CONTENT_TYPE + " TEXT," +
                    COLUMN_AUTHORIZATION + " TEXT," +
                    COLUMN_STATUS + " TEXT," +
                    COLUMN_CORRELATION_ID + " TEXT," +
                    COLUMN_FILES + " TEXT" +
                    ");";

    public OfflineOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(OFFLINE_TABLE_CREATE);
        } catch (Exception e) {
            PreyLogger.e("Error creating table: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + OFFLINE_TABLE_CREATE);
        } catch (Exception e) {
            PreyLogger.e("Erase error table: " + e.getMessage(), e);
        }
        onCreate(db);
    }

    public void insertOffline(OfflineDto dto) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OFFLINE_ID, dto.getOfflineId());
        values.put(COLUMN_URL, dto.getUrl());
        values.put(COLUMN_TYPE, dto.getType());
        values.put(COLUMN_PARAMETERS, dto.getParameters());
        values.put(COLUMN_REQUEST_METHOD, dto.getRequestMethod());
        values.put(COLUMN_CONTENT_TYPE, dto.getContentType());
        values.put(COLUMN_AUTHORIZATION, dto.getAuthorization());
        values.put(COLUMN_STATUS, dto.getStatus());
        values.put(COLUMN_CORRELATION_ID, dto.getCorrelationId());
        values.put(COLUMN_FILES, dto.getFiles());

        PreyLogger.d("___db insert:" + dto.toString());
        database.insert(OFFLINE_TABLE_NAME, null, values);
        database.close();
    }

    public void updateOffline(OfflineDto dto) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_URL, dto.getUrl());
        values.put(COLUMN_TYPE, dto.getType());
        values.put(COLUMN_PARAMETERS, dto.getParameters());
        values.put(COLUMN_REQUEST_METHOD, dto.getRequestMethod());
        values.put(COLUMN_CONTENT_TYPE, dto.getContentType());
        values.put(COLUMN_AUTHORIZATION, dto.getAuthorization());
        values.put(COLUMN_STATUS, dto.getStatus());
        values.put(COLUMN_CORRELATION_ID, dto.getCorrelationId());
        values.put(COLUMN_FILES, dto.getFiles());

        String selection = COLUMN_OFFLINE_ID + " = ?";
        String[] selectionArgs = {dto.getOfflineId()};
        PreyLogger.d("___db update:" + dto.toString());
        database.update(OFFLINE_TABLE_NAME, values, selection, selectionArgs);
        database.close();
    }

    public void deleteOffline(String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + OFFLINE_TABLE_NAME + " where " + COLUMN_OFFLINE_ID + "='" + id + "'";
        PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public void deleteAllOffline() {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + OFFLINE_TABLE_NAME ;
        PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public List<OfflineDto> getAllOffline() {
        List<OfflineDto> list = new ArrayList<OfflineDto>();
        String selectQuery = "SELECT  * FROM " + OFFLINE_TABLE_NAME;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                OfflineDto dto = new OfflineDto();
                dto.setOfflineId(cursor.getString(0));
                dto.setUrl(cursor.getString(1));
                dto.setType(cursor.getString(2));
                dto.setParameters(cursor.getString(3));
                dto.setRequestMethod(cursor.getString(4));
                dto.setContentType(cursor.getString(5));
                dto.setAuthorization(cursor.getString(6));
                dto.setStatus(cursor.getString(7));
                dto.setCorrelationId(cursor.getString(8));
                dto.setFiles(cursor.getString(9));
                list.add(dto);
            } while (cursor.moveToNext());
        }
        return list;
    }

    public OfflineDto getOffline(String id) {
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + OFFLINE_TABLE_NAME + " where " + COLUMN_OFFLINE_ID + "='" + id + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        OfflineDto dto = null;
        if (cursor.moveToFirst()) {
            do {
                dto = new OfflineDto();
                dto.setOfflineId(cursor.getString(0));
                dto.setUrl(cursor.getString(1));
                dto.setType(cursor.getString(2));
                dto.setParameters(cursor.getString(3));
                dto.setRequestMethod(cursor.getString(4));
                dto.setContentType(cursor.getString(5));
                dto.setAuthorization(cursor.getString(6));
                dto.setStatus(cursor.getString(7));
                dto.setCorrelationId(cursor.getString(8));
                dto.setFiles(cursor.getString(9));
            } while (cursor.moveToNext());
        }
        return dto;
    }
}
