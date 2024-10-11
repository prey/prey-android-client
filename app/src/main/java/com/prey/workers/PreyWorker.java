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

/**
 * This class represents a worker that starts a periodic worker to run every 15 minutes, with a 5 minute flex period.
 */
public class PreyWorker {

    private static PreyWorker _instance = null;
    public static final String INCREMENT_WORK_NAME = "prey_increment_work";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private PreyWorker() {
    }

    /**
     * Returns the singleton instance of the PreyWorker.
     *
     * @return the singleton instance of PreyWorker
     */
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
        // Create a PeriodicWorkRequest builder
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(
                IncrementWorker.class,
                15,
                TimeUnit.MINUTES,
                5,
                TimeUnit.MINUTES)
                .addTag(INCREMENT_WORK_NAME);
        // Build the PeriodicWorkRequest
        PeriodicWorkRequest workRequest = builder.build();
        // Enqueue the work request
        workManager.enqueue(workRequest);
    }

}