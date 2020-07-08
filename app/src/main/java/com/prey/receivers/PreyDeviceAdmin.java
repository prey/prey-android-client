/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.WindowManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

import com.prey.PreyPermission;
import com.prey.R;
import com.prey.activities.PasswordActivity;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.exceptions.PreyException;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class PreyDeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PreyLogger.d("LOCK intent.getAction:" + intent.getAction());

        super.onReceive(context,intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        PreyLogger.d("Device Admin enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getText(R.string.preferences_admin_enabled_dialog_message).toString();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        PreyLogger.d("Device Admin disabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        PreyLogger.d("Password was changed successfully");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        boolean isLockSet=PreyConfig.getPreyConfig(context).isLockSet();
        PreyLogger.d("Password onPasswordSucceeded isLockSet:"+isLockSet);

        if (isLockSet){
            PreyLogger.d("Password was entered successfully");
            sendUnLock(context);
        }
    }

    public static void sendUnLock(Context context){
        boolean isLockSet=PreyConfig.getPreyConfig(context).isLockSet();
        PreyLogger.d("sendUnLock isLockSet:"+isLockSet);
        if (isLockSet){
            if(PreyConfig.getPreyConfig(context).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(context)) {
                PreyLogger.d("sendUnLock nothing");
            }else {
                PreyLogger.d("sendUnLock deleteUnlockPass");
                PreyConfig.getPreyConfig(context).setLock(false);
                PreyConfig.getPreyConfig(context).deleteUnlockPass();
                final Context ctx = context;
                new Thread() {
                    public void run() {
                        String jobIdLock = PreyConfig.getPreyConfig(ctx).getJobIdLock();
                        String reason ="{\"origin\":\"user\"}";
                        if (jobIdLock != null && !"".equals(jobIdLock)) {
                            reason = "{\"origin\":\"user\",\"device_job_id\":\"" + jobIdLock + "\"}";
                            PreyConfig.getPreyConfig(ctx).setJobIdLock("");
                        }
                        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped", reason));
                    }
                }.start();
            }
        }
    }

    public static void lockWhenYouNocantDrawOverlays(Context ctx) {
        boolean isLockSet=PreyConfig.getPreyConfig(ctx).isLockSet();
        PreyLogger.d("DeviceAdmin lockWhenYouNocantDrawOverlays isLockSet:" + isLockSet);
        if (isLockSet) {
            if(!PreyPermission.isAccessibilityServiceEnabled(ctx)) {
                if (!canDrawOverlays(ctx)) {
                    boolean isPatternSet = PreyDeviceAdmin.isPatternSet(ctx);
                    boolean isPassOrPinSet = PreyDeviceAdmin.isPassOrPinSet(ctx);
                    PreyLogger.d("CheckLockActivated isPatternSet:" + isPatternSet);
                    PreyLogger.d("CheckLockActivated  isPassOrPinSet:" + isPassOrPinSet);
                    if (isPatternSet || isPassOrPinSet) {
                        FroyoSupport.getInstance(ctx).lockNow();
                        new Thread(new EventManagerRunner(ctx, new Event(Event.NATIVE_LOCK))).start();
                    } else {
                        try {
                            FroyoSupport.getInstance(ctx).changePasswordAndLock(PreyConfig.getPreyConfig(ctx).getUnlockPass(), true);
                            new Thread(new EventManagerRunner(ctx, new Event(Event.NATIVE_LOCK))).start();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    public static void lockOld(Context ctx) {
        boolean isLockSet=PreyConfig.getPreyConfig(ctx).isLockSet();
        PreyLogger.d("DeviceAdmin lockWhenYouNocantDrawOverlays isLockSet:" + isLockSet);
        if (isLockSet) {
            boolean isPatternSet = PreyDeviceAdmin.isPatternSet(ctx);
            boolean isPassOrPinSet = PreyDeviceAdmin.isPassOrPinSet(ctx);
            PreyLogger.d("CheckLockActivated isPatternSet:" + isPatternSet);
            PreyLogger.d("CheckLockActivated  isPassOrPinSet:" + isPassOrPinSet);
            if (isPatternSet || isPassOrPinSet) {
                FroyoSupport.getInstance(ctx).lockNow();
            } else {
                try {
                    FroyoSupport.getInstance(ctx).changePasswordAndLock(PreyConfig.getPreyConfig(ctx).getUnlockPass(), true);
                } catch (Exception e) {
                    PreyLogger.e("error lockold:"+e.getMessage(),e);
                }
            }
        }
    }

    public static boolean canDrawOverlays(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(ctx);
    }

    public boolean isDeviceScreenLocked(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return isDeviceLocked(ctx);
        } else {
            return isPatternSet(ctx) || isPassOrPinSet(ctx);
        }
    }

    /**
     * @return true if pattern set, false if not (or if an issue when checking)
     */
    public static boolean isPatternSet(Context ctx) {
        ContentResolver cr = ctx.getContentResolver();
        try {
            int lockPatternEnable = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED);
            return lockPatternEnable == 1;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    /**
     * @return true if pass or pin set
     */
    @TargetApi(16)
    public static boolean isPassOrPinSet(Context ctx) {
        KeyguardManager keyguardManager = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE); //api 16+
        return keyguardManager.isKeyguardSecure();
    }

    /**
     * @return true if pass or pin or pattern locks screen
     */
    @TargetApi(23)
    private boolean isDeviceLocked(Context ctx) {
        KeyguardManager keyguardManager = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE); //api 23+
        return keyguardManager.isDeviceSecure();
    }

}
