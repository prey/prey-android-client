/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2022 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.prey.activities.OpenSettingsActivity;

public class PreyPermissionService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    /**
     * Service that calls the open activity OpenSettingsActivity
     *
     * @param intent
     * @param startId
     */
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Context ctx = this;
        Intent intentConfiguration = new Intent(ctx, OpenSettingsActivity.class);
        intentConfiguration.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentConfiguration);
    }

}