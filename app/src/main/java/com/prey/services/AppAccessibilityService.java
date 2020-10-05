/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.activities.PasswordNativeActivity;
import com.prey.activities.PasswordHtmlActivity;

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
            boolean isPinActivated = PreyConfig.getPreyConfig(getApplicationContext()).getPinActivated();
            if (isLock||isPinActivated) {
                if (accessibilityEvent != null && accessibilityEvent.getPackageName() != null) {
                    String charSequence = accessibilityEvent.getPackageName() != null ? accessibilityEvent.getPackageName().toString() : null;
                    //String charSequence2 = accessibilityEvent.getClassName() != null ? accessibilityEvent.getClassName().toString() : null;
                    if ("com.prey".equals(charSequence) || "android".equals(charSequence)) {
                    } else {
                        Intent intent = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent = new Intent(getApplicationContext(), PasswordHtmlActivity.class);
                        }else{
                            intent = new Intent(getApplicationContext(), PasswordNativeActivity.class);
                        }
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
