/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import android.content.Context;
import android.location.Location;

import com.prey.PreyLogger;
import com.prey.services.UtilityService;

public class PreyGooglePlayServiceLocation {


    private Location currentLocation = null;

    public void init(Context ctx) {
        PreyLogger.d("init");


        UtilityService.requestLocation(ctx);
    }

    public Location getLastLocation(Context ctx) {
        /*try {
            if (currentLocation == null) {
                currentLocation = UtilityService.getLastLocation();
            }
        } catch (Exception e) {
        }
        */
        PreyLogger.d("getLastLocation is null:" + (currentLocation == null));
        return currentLocation;
    }


}

