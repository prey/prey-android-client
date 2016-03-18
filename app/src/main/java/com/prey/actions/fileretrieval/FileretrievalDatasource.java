/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.geofences.GeofenceDto;
import com.prey.actions.geofences.GeofenceOpenHelper;

import java.util.List;

public class FileretrievalDatasource {


    private FileretrievalOpenHelper dbHelper;

    public FileretrievalDatasource(Context context) {
        dbHelper = new FileretrievalOpenHelper(context);
    }

    public void createGeofence(FileretrievalDto dto) {
        try {
            dbHelper.insertFileretrieval(dto);
        } catch (Exception e) {;
            try {
                dbHelper.updateFileretrieval(dto);
            } catch (Exception e1) {
                PreyLogger.e("error db update:" + e1.getMessage(), e1);
            }
        }
    }

    public void deleteFileretrieval(String id) {
        dbHelper.deleteFileretrieval (id);
    }

    public List<FileretrievalDto> getAllFileretrieval() {
        return dbHelper.getAllFileretrieval();
    }

    public FileretrievalDto getFileretrievals(String id) {
        return dbHelper.getFileretrieval(id);
    }

    public void deleteAllFileretrieval() {
        dbHelper.deleteAllFileretrieval();
    }
}
