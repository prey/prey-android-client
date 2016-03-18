/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.managers.PreyWifiManager;
import com.prey.net.PreyWebServices;

import java.io.File;
import java.util.List;

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
