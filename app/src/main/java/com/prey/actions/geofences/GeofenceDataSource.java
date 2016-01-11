/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences;

import android.content.Context;

import com.prey.PreyLogger;

import java.util.List;
import java.util.Map;

public class GeofenceDataSource {

    private GeofenceOpenHelper dbHelper;

    public GeofenceDataSource(Context context) {
        dbHelper = new GeofenceOpenHelper(context);
    }

    public void createGeofence(GeofenceDto geofence) {
        try {
            dbHelper.insertGeofence(geofence);
        } catch (Exception e) {;
            try {
                dbHelper.updateGeofence(geofence);
            } catch (Exception e1) {
                PreyLogger.e("error db update:" + e1.getMessage(), e1);
            }
        }
    }

    public void deleteGeofence(String id) {
        dbHelper.deleteGeofence(id);
    }

    public List<GeofenceDto> getAllGeofences() {
        return dbHelper.getAllGeofences();
    }

    public GeofenceDto getGeofences(String id) {
        return dbHelper.getGeofence(id);
    }

    public void deleteAllGeofence() {
        dbHelper.deleteAllGeofence();
    }

}