package com.prey.ble.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.prey.PreyLogger;

import java.util.ArrayList;
import java.util.List;

public class KeyOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_NAME = "KeyBle.db";

    public static final String KEY_BLE_TABLE_NAME = "key_ble";

    public static final String COLUMN_ADDRESS = "_address";
    public static final String COLUMN_NAME = "_name";
    public static final String COLUMN_ALIAS = "_alias";
    public static final String COLUMN_IMAGE = "_image";


    private static final String KEY_BLE_TABLE_CREATE =
            "CREATE TABLE " + KEY_BLE_TABLE_NAME + " (" +
                    COLUMN_ADDRESS + " TEXT PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_ALIAS + " REAL," +
                    COLUMN_IMAGE + " INTEGER" +
                    ");";

    public KeyOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(KEY_BLE_TABLE_CREATE);
        } catch (Exception e) {
            PreyLogger.e("Error creating table: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + KEY_BLE_TABLE_NAME);
        } catch (Exception e) {
            PreyLogger.e("Erase error table: " + e.getMessage(), e);
        }
        onCreate(db);
    }

    public void insertKey(KeyDto key) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS, key.getAddress());
        values.put(COLUMN_NAME, key.getName());
        values.put(COLUMN_ALIAS, key.getAlias());
        values.put(COLUMN_IMAGE, key.getImage());
        PreyLogger.d("___db insert:" + key.toString());
        database.insert(KEY_BLE_TABLE_NAME, null, values);
        database.close();
    }

    public void updateKey(KeyDto key) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, key.getName());
        values.put(COLUMN_ALIAS, key.getAlias());
        values.put(COLUMN_IMAGE, key.getImage());
        String selection = COLUMN_ADDRESS + " = ?";
        String[] selectionArgs = {key.getAddress()};
        PreyLogger.d("___db update:" + key.toString());
        database.update(KEY_BLE_TABLE_NAME, values, selection, selectionArgs);
        database.close();
    }



    public void deleteKey(String address) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + KEY_BLE_TABLE_NAME + " where " + COLUMN_ADDRESS + "='" + address + "'";
        PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public void deleteAllKeys() {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM  " + KEY_BLE_TABLE_NAME ;
        PreyLogger.d("query" + deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public List<KeyDto> getAllKeys() {
        Cursor cursor =null;
        List<KeyDto> list = new ArrayList<KeyDto>();
        try {
            String selectQuery = "SELECT  * FROM " + KEY_BLE_TABLE_NAME;
            SQLiteDatabase database = this.getReadableDatabase();
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    KeyDto key = new KeyDto();
                    key.setAddress(cursor.getString(0));
                    key.setName(cursor.getString(1));
                    key.setAlias(cursor.getString(2));
                    key.setImage(cursor.getInt(3));

                    list.add(key);
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

    public KeyDto getKey(String address) {
        Cursor cursor =null;
        KeyDto key = null;
        try {
            SQLiteDatabase database = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + KEY_BLE_TABLE_NAME + " where " + COLUMN_ADDRESS + "='" + address + "'";
            cursor = database.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    key = new KeyDto();
                    key.setAddress(cursor.getString(0));
                    key.setName(cursor.getString(1));
                    key.setAlias(cursor.getString(2));
                    key.setImage(cursor.getInt(3));
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            PreyLogger.e("error:"+e.getMessage(),e);
        }finally {
            if(cursor!=null){
                try{cursor.close();}catch (Exception e1){}
            }
        }
        return key;
    }
}


