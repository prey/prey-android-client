package com.prey.ble.db;


import android.content.Context;
import android.util.Log;

import com.prey.PreyLogger;

import java.util.List;

public class KeyDatasource {

    private KeyOpenHelper dbHelper;

    public KeyDatasource(Context context) {
        dbHelper = new KeyOpenHelper(context);
    }

    public void createKey(KeyDto key) {
        try {
            dbHelper.insertKey(key);
        } catch (Exception e) {;
            try {
                dbHelper.updateKey(key);
            } catch (Exception e1) {
                PreyLogger.e("error db update:" + e1.getMessage(), e1);
            }
        }
    }

    public void deleteKey(String id) {
        dbHelper.deleteKey(id);
    }

    public List<KeyDto> getAllKeys() {
        return dbHelper.getAllKeys();
    }

    public KeyDto getKey(String id) {
        return dbHelper.getKey(id);
    }

    public void deleteAllKeys() {
        dbHelper.deleteAllKeys();
    }

}

