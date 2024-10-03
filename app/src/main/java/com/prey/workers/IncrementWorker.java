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

public class IncrementWorker extends Worker {

    public IncrementWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        PreyLogger.d("AWARE WORK doWork");
        Context context = getApplicationContext();
        try {
            if (PreyConfig.getPreyConfig(context).isTimeNextAware()) {
                new Thread() {
                    public void run() {
                        new AwareController().init(context);
                    }
                }.start();
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