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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.actions.Lock;
import com.prey.services.CheckLockActivated;
import com.prey.services.PreyLockHtmlService;
import com.prey.services.PreyLockService;

public class LoginActivity extends Activity {
    private static final String EXTRA_LAUNCHED_AS_SETUP_ACTION =
            "com.google.android.apps.work.clouddpc.EXTRA_LAUNCHED_AS_SETUP_ACTION";

    // Guard against startup() launching SplashMdmActivity multiple times when
    // onCreate/onStart/onResume all fire in rapid succession before the splash
    // has had a chance to actually start. Without this, the user sees the
    // splash open 2-3 times for a single tap of "Open Prey".
    private boolean mdmSplashLaunched = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("calling_activity_null", getCallingActivity() == null);
        info.put("calling_activity",
                getCallingActivity() != null ? getCallingActivity().flattenToShortString() : "null");
        info.put("calling_package", getCallingPackage() != null ? getCallingPackage() : "null");
        info.put("referrer", getReferrer() != null ? getReferrer().toString() : "null");
        info.put("intent_flags", getIntent() != null ? Integer.toHexString(getIntent().getFlags()) : "null");
        info.put("launched_as_setup_extra",
                getIntent() != null && getIntent().getBooleanExtra(EXTRA_LAUNCHED_AS_SETUP_ACTION, false));
        com.prey.mdm.MdmDebugReporter.send(this, "login_oncreate", info);
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

    private int mdmRetryCount = 0;
    private static final int MDM_MAX_RETRIES = 10;
    private static final int MDM_RETRY_DELAY_MS = 2000;

    private void startup() {
        Intent intentLock = null;
        String unlockPass = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
        if (unlockPass != null && !unlockPass.isEmpty()) {
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(getApplicationContext());
            boolean accessibility = PreyPermission.isAccessibilityServiceEnabled(getApplicationContext());
            if (PreyConfig.getPreyConfig(getApplicationContext()).isMarshmallowOrAbove() &&
                    (canDrawOverlays||accessibility)) {
                PreyLogger.d("Login Boot finished. PreyLockService");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PreyLogger.d("login 2");
                    intentLock = new Intent(getApplicationContext(), PreyLockHtmlService.class);
                } else {
                    PreyLogger.d("login 3");
                    intentLock = new Intent(getApplicationContext(), PreyLockService.class);
                }
                getApplicationContext().startService(intentLock);
                getApplicationContext().startService(new Intent(getApplicationContext(), CheckLockActivated.class));
            } else {
                Lock.lockWhenYouNocantDrawOverlays(getApplicationContext());
            }
        }
        boolean ready = PreyConfig.getPreyConfig(this).getProtectReady();
        if (!ready && hasMdmSetupKey()) {
            showMdmSplash();
        } else if (!ready && isProvisioningSetupAction() && mdmRetryCount < MDM_MAX_RETRIES) {
            // SetupAction launched during provisioning but managed config not yet available — retry
            mdmRetryCount++;
            PreyLogger.d(String.format("LoginActivity: waiting for managed config (retry %d/%d)", mdmRetryCount, MDM_MAX_RETRIES));
            new android.os.Handler().postDelayed(this::startup, MDM_RETRY_DELAY_MS);
        } else if (isThereBatchInstallationKey() && !ready) {
            showLoginBatch();
        } else {
            showLogin();
        }
    }

    private boolean isProvisioningSetupAction() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(EXTRA_LAUNCHED_AS_SETUP_ACTION, false)) {
            return true;
        }
        return getCallingActivity() != null;
    }

    private void showLogin() {
        Intent intent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(LoginActivity.this, CheckPasswordHtmlActivity.class);
        } else {
            boolean registered = PreyConfig.getPreyConfig(this).isThisDeviceAlreadyRegisteredWithPrey();
            if (registered) {
                intent = new Intent(LoginActivity.this, CheckPasswordActivity.class);
            } else {
                boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
                PreyLogger.d(String.format("LoginActivity: canDrawOverlays:%b", canDrawOverlays));
                boolean isAdminActive = FroyoSupport.getInstance(this).isAdminActive();
                PreyLogger.d(String.format("LoginActivity: isAdminActive:%b", isAdminActive));
                boolean configurated = canDrawOverlays && isAdminActive;
                if (configurated) {
                    intent = new Intent(LoginActivity.this, SignInActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, OnboardingActivity.class);
                }
            }
        }
        if (PreyConfig.getPreyConfig(this).isChromebook()) {
            intent = new Intent(LoginActivity.this, ChromeActivity.class);
        }
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

    private void showFeedback(Context ctx) {
        Intent popup = new Intent(ctx, FeedbackActivity.class);
        popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(popup);
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
                return setupKey != null && !setupKey.isEmpty();
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error hasMdmSetupKey: %s", e.getMessage()), e);
        }
        return false;
    }

    private static final int MDM_SETUP_REQUEST = 100;

    private void showMdmSplash() {
        if (mdmSplashLaunched) {
            return;
        }
        mdmSplashLaunched = true;
        Intent intent = new Intent(LoginActivity.this, SplashMdmActivity.class);
        if (isProvisioningSetupAction()) {
            intent.putExtra(EXTRA_LAUNCHED_AS_SETUP_ACTION, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
            return;
        }
        startActivityForResult(intent, MDM_SETUP_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("request_code", requestCode);
        info.put("result_code", resultCode);
        info.put("result_ok", resultCode == RESULT_OK);
        info.put("calling_activity_null", getCallingActivity() == null);
        info.put("calling_activity",
                getCallingActivity() != null ? getCallingActivity().flattenToShortString() : "null");
        com.prey.mdm.MdmDebugReporter.send(this, "login_onactivityresult", info);
        if (requestCode == MDM_SETUP_REQUEST) {
            mdmSplashLaunched = false; // reset for any re-entries
            if (resultCode == RESULT_OK) {
                // Propagate result to provisioning setup wizard (SetupAction)
                setResult(RESULT_OK);
                com.prey.mdm.MdmDebugReporter.send(this, "login_propagating_result_ok");
                finish();
            }
        }
    }

    private boolean isThereBatchInstallationKey() {
        String apiKeyBatch = PreyConfig.getPreyConfig(LoginActivity.this).getApiKeyBatch();
        return (apiKeyBatch != null && !apiKeyBatch.isEmpty());
    }

}
