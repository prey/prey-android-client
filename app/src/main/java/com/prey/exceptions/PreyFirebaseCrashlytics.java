/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2023 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.exceptions;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import com.prey.PreyConfig;

public class PreyFirebaseCrashlytics {

    private static PreyFirebaseCrashlytics instance = null;
    private FirebaseCrashlytics crashlytics = null;

    private PreyFirebaseCrashlytics(Context ctx) {
        //initialize FirebaseCrashlytics
        FirebaseApp.initializeApp(ctx);
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCustomKey("devicekey", PreyConfig.getPreyConfig(ctx).getDeviceId());
        crashlytics.setCustomKey("apikey", PreyConfig.getPreyConfig(ctx).getApiKey());
    }

    public static PreyFirebaseCrashlytics getInstance(Context ctx) {
        if (instance == null)
            instance = new PreyFirebaseCrashlytics(ctx);
        return instance;
    }

    /**
     * Method to report an exception
     */
    public void recordException(@NonNull Throwable throwable) {
        crashlytics.recordException(throwable);
    }
}
