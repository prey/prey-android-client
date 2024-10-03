/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/

package com.prey.workers;

import android.content.Context;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class PreyWorker {

    private static PreyWorker _instance = null;
    public static final String INCREMENT_WORK_NAME = "prey_increment_work";

    private PreyWorker() {
    }

    public static PreyWorker getInstance() {
        if (_instance == null)
            _instance = new PreyWorker();
        return _instance;
    }

    /**
     * Starts a periodic worker to run every 15 minutes, with a 5 minute flex period.
     * @param context the application context
     */
    public void startPeriodicWork(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(INCREMENT_WORK_NAME);
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                IncrementWorker.class,
                15,
                TimeUnit.MINUTES,
                5,
                TimeUnit.MINUTES)
                .addTag(INCREMENT_WORK_NAME)
                .build();
        workManager.enqueue(workRequest);
    }

}