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
import com.prey.activities.PasswordHtmlActivity;
import com.prey.activities.PasswordNativeActivity;

public class AppAccessibilityService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
        PreyLogger.d("AppAccessibilityService onCreate");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        try {
            String unlockPass=PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
            boolean isLock = unlockPass!=null && !"".equals(unlockPass);
            if ( isLock ) {
                PreyLogger.d("acc 1");
                if (accessibilityEvent != null && accessibilityEvent.getPackageName() != null) {
                    String charSequence = accessibilityEvent.getPackageName() != null ? accessibilityEvent.getPackageName().toString() : null;
                    if ("com.prey".equals(charSequence) || "android".equals(charSequence)) {
                    } else {
                        if(isLock) {
                            PreyLogger.d("acc 2");
                            Intent intentPasswordActivity = null;
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                intentPasswordActivity = new Intent(getApplicationContext(), PasswordHtmlActivity.class);
                            }else{
                                intentPasswordActivity = new Intent(getApplicationContext(), PasswordNativeActivity.class);
                            }
                            intentPasswordActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intentPasswordActivity);
                        }
                    }
                }
            }
        }catch (Exception e){
            PreyLogger.e("Error onAccessibilityEvent:"+e.getMessage(),e);
        }
    }

    @Override
    public void onInterrupt() {
    }

}