/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval;

import android.app.IntentService;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

public class FileretrievalService extends IntentService {

    public FileretrievalService() {
        super(PreyConfig.TAG);
    }

    public FileretrievalService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PreyLogger.d("***************onHandleIntent");
        FileretrievalController.getInstance().run(getApplicationContext());
        stopSelf();
    }

}
