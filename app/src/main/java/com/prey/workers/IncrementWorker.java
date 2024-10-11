/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/

package com.prey.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.aware.AwareController;

/**
 * A Worker class responsible for incrementing a value and handling AwareController initialization.
 */
public class IncrementWorker extends Worker {

    /**
     * Constructor for IncrementWorker.
     *
     * @param context  the application context
     * @param params   the worker parameters
     */
    public IncrementWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Performs the work for this Worker.
     *
     * @return Result.success() if the work is successful, Result.failure() otherwise
     */
    @Override
    public Result doWork() {
        // Log a debug message to indicate the start of the work
        PreyLogger.d("AWARE WORK doWork");
        // Get the application context
        Context context = getApplicationContext();
        try {
            // Check if it's time to run the AwareController
            if (PreyConfig.getPreyConfig(context).isTimeNextAware()) {
                // Create a new thread to run the AwareController initialization
                new Thread() {
                    public void run() {
                        // Initialize the AwareController
                        new AwareController().init(context);
                    }
                }.start(); // Start the thread
            }
            return Result.success();
        } catch (NumberFormatException e) {
            PreyLogger.e(String.format("----------Error IncrementWorker:%s", e.getMessage()), e);
            return Result.failure();
        } catch (Throwable throwable) {
            PreyLogger.e(String.format("----------Error IncrementWorker:%s", throwable.getMessage()), throwable);
            return Result.failure();
        }
    }

}