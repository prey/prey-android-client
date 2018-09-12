/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.net.PreyWebServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeofecenceParse {

    public static List<GeofenceDto> getJSONFromUrl(Context ctx) {
        String json = null;
        try {
            json = PreyWebServices.getInstance().geofencing(ctx);
        } catch (Exception e) {
        }
        return getJSONFromTxt(ctx, json);
    }

    public static List<GeofenceDto> getJSONFromTxt(Context ctx, String json) {
        json = "{\"prey\":" + json + "}";
        List<GeofenceDto> listGeofence = new ArrayList<GeofenceDto>();
        PreyLogger.d(json);
        try {
            JSONObject jsnobject = new JSONObject(json);
            JSONArray jsonArray = jsnobject.getJSONArray("prey");
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonCommand = jsonArray.get(i).toString();
                JSONObject explrObject = new JSONObject(jsonCommand);
                GeofenceDto geofence = new GeofenceDto();
                geofence.id = explrObject.getString("id");
                geofence.name = explrObject.getString("name");
                geofence.latitude = Double.parseDouble(explrObject.getString("lat"));
                geofence.longitude = Double.parseDouble(explrObject.getString("lng"));
                geofence.radius = Float.parseFloat(explrObject.getString("radius"));
                listGeofence.add(geofence);
            }
        } catch (Exception e) {
            return null;
        }
        return listGeofence;
    }
}