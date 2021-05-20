/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.content.Intent;
import android.app.IntentService;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

public class ActivityIntentService extends IntentService {

    public ActivityIntentService() {
        super(PreyConfig.TAG);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        PreyLogger.d("ActivityIntentService onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

}