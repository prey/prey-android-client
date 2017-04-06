/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.offline;

import android.content.Context;

import com.prey.PreyLogger;

import java.util.List;

public class OfflineDatasource {

    private OfflineOpenHelper dbHelper;

    public OfflineDatasource(Context context) {
        dbHelper = new OfflineOpenHelper(context);
    }

    public void createOffline(OfflineDto dto) {
        try {
            dbHelper.insertOffline(dto);
        } catch (Exception e) {
            try {
                dbHelper.updateOffline(dto);
            } catch (Exception e1) {
                PreyLogger.e("error db update:" + e1.getMessage(), e1);
            }
        }
    }

    public void deleteOffline(String id) {
        dbHelper.deleteOffline(id);
    }

    public List<OfflineDto> getAllOffline() {
        return dbHelper.getAllOffline();
    }

    public OfflineDto getOffline(String id) {
        return dbHelper.getOffline(id);
    }

    public void deleteAllOffline() {
        dbHelper.deleteAllOffline();
    }

}
