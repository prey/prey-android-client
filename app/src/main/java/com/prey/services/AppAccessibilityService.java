/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.activities.PasswordActivity2;

public class AppAccessibilityService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
        PreyLogger.d("AppAccessibilityService onCreate");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        try {
            boolean isLock = PreyConfig.getPreyConfig(getApplicationContext()).isLockSet();
            if (isLock) {
                PreyLogger.d("AppAccessibilityService onAccessibilityEvent");
                if (accessibilityEvent != null && accessibilityEvent.getPackageName() != null) {
                    String charSequence = accessibilityEvent.getPackageName() != null ? accessibilityEvent.getPackageName().toString() : null;
                    String charSequence2 = accessibilityEvent.getClassName() != null ? accessibilityEvent.getClassName().toString() : null;
                    PreyLogger.d("AppAccessibilityService charSequence:" + charSequence + " charSequence2:" + charSequence2);
                    if ("com.prey".equals(charSequence) || "android".equals(charSequence)) {
                    } else {
                        PreyLogger.d("AppAccessibilityService isLock:" + isLock);
                        Intent intent = new Intent(getApplicationContext(), PasswordActivity2.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                }
            }
        }catch (Exception e){
        }
    }

    @Override
    public void onInterrupt() {

    }
}
