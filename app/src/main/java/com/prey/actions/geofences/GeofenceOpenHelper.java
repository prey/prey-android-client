/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.prey.PreyLogger;

import java.util.ArrayList;
import java.util.List;

public class GeofenceOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;

    private static final String DATABASE_NAME = "Geofence.db";

    public static final String GEOFENCE_TABLE_NAME = "geofence";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "_name";
    public static final String COLUMN_LATITUDE = "_latitude";
    public static final String COLUMN_LONGITUDE = "_longitude";
    public static final String COLUMN_RADIUS = "_radius";
    public static final String COLUMN_TYPE = "_type";
    public static final String COLUMN_EXPIRES = "_expires";

    private static final String GEOFENCE_TABLE_CREATE =
            "CREATE TABLE " + GEOFENCE_TABLE_NAME + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_LATITUDE + " REAL," +
                    COLUMN_LONGITUDE + " REAL," +
                    COLUMN_RADIUS + " REAL," +
                    COLUMN_TYPE + " TEXT," +
                    COLUMN_EXPIRES + " INTEGER" +
                    ");";

    public GeofenceOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(GEOFENCE_TABLE_CREATE);
        } catch (Exception e) {
            PreyLogger.e("Error creating table: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + GEOFENCE_TABLE_NAME);
        } catch (Exception e) {
            PreyLogger.e("Erase error table: " + e.getMessage(), e);
        }
        onCreate(db);
    }

    public void insertGeofence(GeofenceDto geofence) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, geofence.getId());
        values.put(COLUMN_NAME, geofence.getName());
        values.put(COLUMN_LATITUDE, geofence.getLatitude());
        values.put(COLUMN_LONGITUDE, geofence.getLongitude());
        values.put(COLUMN_RADIUS, geofence.getRadius());
        values.put(COLUMN_EXPIRES, geofence.getExpires());
        PreyLogger.d("___db insert:" + geofence.toString());
        database.insert(GEOFENCE_TABLE_NAME, null, values);
        database.close();
    }

    public void updateGeofence(GeofenceDto geofence) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, geofence.getName());
        values.put(COLUMN_LATITUDE, geofence.getLatitude());
        values.put(COLUMN_LONGITUDE, geofence.getLongitude());
        values.put(COLUMN_RADIUS, geofence.getRadius());
        values.put(COLUMN_EXPIRES, geofence.getExpires());
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {geofence.getId()};
        //PreyLogger.d("___db update:" + geofence.toString());
        database.update(GEOFENCE_TABLE_NAME, values, selection, selectionArgs);
        database.close();
    }

    public void updateGeofenceType(String id,String type) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE,type);
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {id};
        //PreyLogger.d("___db update type:"+type+" id:" + id);
        database.update(GEOFENCE_TABLE_NAME, values, selection, selectionArgs);
        database.close();
    }


    public void deleteGeofence(String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + GEOFENCE_TABLE_NAME + " where " + COLUMN_ID + "='" + id + "'";
        //PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public void deleteAllGeofence() {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + GEOFENCE_TABLE_NAME ;
        //PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public List<GeofenceDto> getAllGeofences() {
        Cursor cursor =null;
        List<GeofenceDto> list = new ArrayList<GeofenceDto>();
        try {
            String selectQuery = "SELECT  * FROM " + GEOFENCE_TABLE_NAME;
            SQLiteDatabase database = this.getReadableDatabase();
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    GeofenceDto geofence = new GeofenceDto();
                    geofence.setId(cursor.getString(0));
                    geofence.setName(cursor.getString(1));
                    geofence.setLatitude(cursor.getDouble(2));
                    geofence.setLongitude(cursor.getDouble(3));
                    geofence.setRadius(cursor.getFloat(4));
                    geofence.setType(cursor.getString(5));
                    geofence.setExpires(cursor.getInt(6));
                    list.add(geofence);
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            PreyLogger.e("error:"+e.getMessage(),e);
        }finally {
            if(cursor!=null){
                try{cursor.close();}catch (Exception e1){}
            }
        }
        return list;
    }

    public GeofenceDto getGeofence(String id) {
        Cursor cursor =null;
        GeofenceDto geofence = null;
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + GEOFENCE_TABLE_NAME + " where " + COLUMN_ID + "='" + id + "'";
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    geofence = new GeofenceDto();
                    geofence.setId(cursor.getString(0));
                    geofence.setName(cursor.getString(1));
                    geofence.setLatitude(cursor.getDouble(2));
                    geofence.setLongitude(cursor.getDouble(3));
                    geofence.setRadius(cursor.getFloat(4));
                    geofence.setType(cursor.getString(5));
                    geofence.setExpires(cursor.getInt(6));
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            PreyLogger.e("error:"+e.getMessage(),e);
        }finally {
            if(cursor!=null){
                try{cursor.close();}catch (Exception e1){}
            }
        }
        return geofence;
    }
}

