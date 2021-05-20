/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.prey.PreyLogger;

import java.util.ArrayList;
import java.util.List;

public class FileretrievalOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Fileretrieval.db";
    public static final String FILERETRIEVAL_TABLE_NAME = "fileretrieval";
    public static final String COLUMN_FILEID = "_file_id";
    public static final String COLUMN_PATH = "_path";
    public static final String COLUMN_SIZE = "_size";
    public static final String COLUMN_STATUS = "_status";

    private static final String FILERETRIEVAL_TABLE_CREATE =
            "CREATE TABLE " + FILERETRIEVAL_TABLE_NAME + " (" +
                    COLUMN_FILEID + " TEXT PRIMARY KEY, " +
                    COLUMN_PATH + " TEXT," +
                    COLUMN_SIZE + " REAL," +
                    COLUMN_STATUS + " INTEGER" +
                    ");";

    public FileretrievalOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(FILERETRIEVAL_TABLE_CREATE);
        } catch (Exception e) {
            PreyLogger.e("Error creating table: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + FILERETRIEVAL_TABLE_NAME);
        } catch (Exception e) {
            PreyLogger.e("Erase error table: " + e.getMessage(), e);
        }
        onCreate(db);
    }

    public void insertFileretrieval(FileretrievalDto dto) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILEID, dto.getFileId());
        values.put(COLUMN_PATH, dto.getPath());
        values.put(COLUMN_SIZE, dto.getSize());
        values.put(COLUMN_STATUS, dto.getStatus());
        PreyLogger.d("___db insert:" + dto.toString());
        database.insert(FILERETRIEVAL_TABLE_NAME, null, values);
        database.close();
    }

    public void updateFileretrieval(FileretrievalDto dto) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATH, dto.getPath());
        values.put(COLUMN_SIZE, dto.getSize());
        values.put(COLUMN_STATUS, dto.getStatus());
        String selection = COLUMN_FILEID + " = ?";
        String[] selectionArgs = {dto.getFileId()};
        PreyLogger.d("___db update:" + dto.toString());
        database.update(FILERETRIEVAL_TABLE_NAME, values, selection, selectionArgs);
        database.close();
    }

    public void deleteFileretrieval(String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + FILERETRIEVAL_TABLE_NAME + " where " + COLUMN_FILEID + "='" + id + "'";
        PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public void deleteAllFileretrieval() {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + FILERETRIEVAL_TABLE_NAME ;
        PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public List<FileretrievalDto> getAllFileretrieval() {
        Cursor cursor =null;
        List<FileretrievalDto> list = new ArrayList<FileretrievalDto>();
        try {
            String selectQuery = "SELECT  * FROM " + FILERETRIEVAL_TABLE_NAME;
            SQLiteDatabase database = this.getReadableDatabase();
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    FileretrievalDto dto = new FileretrievalDto();
                    dto.setFileId(cursor.getString(0));
                    dto.setPath(cursor.getString(1));
                    dto.setSize(cursor.getLong(2));
                    dto.setStatus(cursor.getInt(3));
                    list.add(dto);
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }finally {
            if(cursor!=null){
                try{cursor.close();}catch (Exception e1){}
            }
        }
        return list;
    }

    public FileretrievalDto getFileretrieval(String id) {
        Cursor cursor =null;
        FileretrievalDto dto = null;
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + FILERETRIEVAL_TABLE_NAME + " where " + COLUMN_FILEID + "='" + id + "'";
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    dto = new FileretrievalDto();
                    dto.setFileId(cursor.getString(0));
                    dto.setPath(cursor.getString(1));
                    dto.setSize(cursor.getLong(2));
                    dto.setStatus(cursor.getInt(3));
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }finally {
            if(cursor!=null){
                try{cursor.close();}catch (Exception e1){}
            }
        }
        return dto;
    }

}