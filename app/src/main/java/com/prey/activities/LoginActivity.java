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
import android.content.RestrictionsManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.json.actions.Lock;
import com.prey.services.CheckLockActivated;
import com.prey.services.PreyLockHtmlService;

public class LoginActivity extends Activity {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        Intent intentLock = null;
        String unlockPass = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
        if (unlockPass != null && !"".equals(unlockPass)) {
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(getApplicationContext());
            boolean accessibility = PreyPermission.isAccessibilityServiceEnabled(getApplicationContext());
            if (canDrawOverlays || accessibility) {
                PreyLogger.d("Login Boot finished. PreyLockService");
                intentLock = new Intent(getApplicationContext(), PreyLockHtmlService.class);
                getApplicationContext().startService(intentLock);
                getApplicationContext().startService(new Intent(getApplicationContext(), CheckLockActivated.class));
            } else {
                Lock.lockWhenYouNocantDrawOverlays(getApplicationContext());
            }
        }
        boolean ready = PreyConfig.getPreyConfig(this).getProtectReady();
        if (!ready && hasMdmSetupKey()) {
            showMdmSplash();
        } else if (isThereBatchInstallationKey() && !ready) {
            showLoginBatch();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        Intent intent = new Intent(LoginActivity.this, CheckPasswordHtmlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoginBatch() {
        Intent intent = null;
        intent = new Intent(LoginActivity.this, SplashBatchActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isThisDeviceAlreadyRegisteredWithPrey() {
        return PreyConfig.getPreyConfig(LoginActivity.this).isThisDeviceAlreadyRegisteredWithPrey(false);
    }

    private boolean hasMdmSetupKey() {
        if (PreyConfig.getPreyConfig(this).isThisDeviceAlreadyRegisteredWithPrey()) {
            return false;
        }
        try {
            RestrictionsManager manager = (RestrictionsManager) getSystemService(Context.RESTRICTIONS_SERVICE);
            Bundle restrictions = manager.getApplicationRestrictions();
            if (restrictions != null && restrictions.containsKey("setup_key")) {
                String setupKey = restrictions.getString("setup_key");
                return setupKey != null && !"".equals(setupKey);
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error hasMdmSetupKey: %s", e.getMessage()), e);
        }
        return false;
    }

    private void showMdmSplash() {
        Intent intent = new Intent(LoginActivity.this, SplashMdmActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isThereBatchInstallationKey() {
        String apiKeyBatch = PreyConfig.getPreyConfig(LoginActivity.this).getApiKeyBatch();
        return (apiKeyBatch != null && !"".equals(apiKeyBatch));
    }

}