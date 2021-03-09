/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.beta.actions.PreyBetaController;
import com.prey.json.actions.Lock;
import com.prey.services.CheckLockActivated;
import com.prey.services.PreyLockHtmlService;
import com.prey.services.PreyLockService;

public class LoginActivity extends Activity {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        startup();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startup();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startup();
    }

    private void startup() {
        Intent intent = null;
        String unlockPass = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
        if (unlockPass != null && !"".equals(unlockPass)) {
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(getApplicationContext());
            //TODO:ACCESS
            //boolean accessibility = PreyPermission.isAccessibilityServiceEnabled(getApplicationContext());
            //if (PreyConfig.getPreyConfig(getApplicationContext()).isMarshmallowOrAbove() &&
            //        (accessibility || canDrawOverlays)) {
            if (PreyConfig.getPreyConfig(getApplicationContext()).isMarshmallowOrAbove() &&
                    (canDrawOverlays)) {
                PreyLogger.d("Login Boot finished. PreyLockService");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PreyLogger.d("login 2");
                    intent = new Intent(getApplicationContext(), PreyLockHtmlService.class);
                } else {
                    PreyLogger.d("login 3");
                    intent = new Intent(getApplicationContext(), PreyLockService.class);
                }
                getApplicationContext().startService(intent);
                getApplicationContext().startService(new Intent(getApplicationContext(), CheckLockActivated.class));
            } else {
                Lock.lockWhenYouNocantDrawOverlays(getApplicationContext());
            }
        }
        boolean ready = PreyConfig.getPreyConfig(this).getProtectReady();
        if (isThereBatchInstallationKey() && !ready) {
            showLoginBatch();
        } else {
            showLogin();
        }
        try{
            PreyBetaController.startPrey(getApplicationContext());
        }catch(Exception e){
        }
    }

    private void showLogin() {
        Intent intent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(LoginActivity.this, CheckPasswordHtmlActivity.class);
        } else {
            String deviceKey = PreyConfig.getPreyConfig(this).getDeviceId();
            if (deviceKey != null && deviceKey != "") {
                intent = new Intent(LoginActivity.this, CheckPasswordActivity.class);
            } else {
                boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
                PreyLogger.d("LoginActivity: canDrawOverlays:" + canDrawOverlays);
                boolean isAdminActive = FroyoSupport.getInstance(this).isAdminActive();
                PreyLogger.d("LoginActivity: isAdminActive:" + isAdminActive);
                boolean configurated = canDrawOverlays && isAdminActive;
                if (configurated) {
                    intent = new Intent(LoginActivity.this, SignInActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, OnboardingActivity.class);
                }
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoginBatch() {
        Intent intent = null;
        intent = new Intent(LoginActivity.this, WelcomeBatchActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isThisDeviceAlreadyRegisteredWithPrey() {
        return PreyConfig.getPreyConfig(LoginActivity.this).isThisDeviceAlreadyRegisteredWithPrey(false);
    }

    private void showFeedback(Context ctx) {
        Intent popup = new Intent(ctx, FeedbackActivity.class);
        popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(popup);
    }

    private boolean isThereBatchInstallationKey() {
        String apiKeyBatch = PreyConfig.getPreyConfig(LoginActivity.this).getApiKeyBatch();
        return (apiKeyBatch != null && !"".equals(apiKeyBatch));
    }

}

