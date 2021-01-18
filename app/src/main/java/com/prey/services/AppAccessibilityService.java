/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

public class AppAccessibilityService {
/*
 extends AccessibilityService {

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
            boolean canDrawOverlays= PreyPermission.canDrawOverlays(getApplicationContext());
            if (!canDrawOverlays && isLock ) {
                PreyLogger.d("acc 1");
                if (accessibilityEvent != null && accessibilityEvent.getPackageName() != null) {
                    String charSequence = accessibilityEvent.getPackageName() != null ? accessibilityEvent.getPackageName().toString() : null;
                    if ("com.prey".equals(charSequence) || "android".equals(charSequence)) {
                    } else {
                        if(isLock) {
                            PreyLogger.d("acc 2");
                            Intent intent = null;
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                intent = new Intent(getApplicationContext(), PasswordHtmlActivity.class);
                            }else{
                                intent = new Intent(getApplicationContext(), PasswordNativeActivity.class);
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        }
                    }
                }
            }
        }catch (Exception e){
        }
    }

    @Override
    public void onInterrupt() {
    }

*/
}