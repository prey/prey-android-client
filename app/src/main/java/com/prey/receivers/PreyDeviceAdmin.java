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
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.exceptions.PreyException;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import java.util.Calendar;
import java.util.Date;

public class PreyDeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PreyLogger.d("LOCK intent.getAction:" + intent.getAction());
        if (intent.getAction().equals(ACTION_DEVICE_ADMIN_DISABLE_REQUESTED ) || intent.getAction().equals(ACTION_DEVICE_ADMIN_DISABLED)) {
            PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
            boolean isBlockAppUninstall= PreyConfig.getPreyConfig(context).isBlockAppUninstall();
            PreyLogger.d("LOCK isBlockAppUninstall:" + isBlockAppUninstall);
            Date now =new Date();
            if(isBlockAppUninstall) {
                boolean active=true;
                long timeBlockAppUninstall=preyConfig.getTimeBlockAppUninstall(  );
                if(timeBlockAppUninstall>0){
                    long timeNow=now.getTime()/1000;
                    long diff=timeBlockAppUninstall-timeNow;
                    PreyLogger.d("LOCK diff:" + diff);
                    if(diff>0){
                        active=false;
                    }
                }
                if(active) {
                    boolean isDeviceScreenLocked = isDeviceScreenLocked(context);
                    PreyLogger.d("LOCK isDeviceScreenLocked:" + isDeviceScreenLocked);
                    if (!isDeviceScreenLocked) {
                        if (preyConfig.isFroyoOrAbove()) {
                            preyConfig.setLock(true);
                            try {
                                String pinNumber=PreyConfig.getPreyConfig(context).getPinNumber();
                                FroyoSupport.getInstance(context).changePasswordAndLock(pinNumber, true);
                            } catch (PreyException e) {
                            }
                        }
                    }
                    DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    policyManager.lockNow();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(now);
                    cal.add(Calendar.MINUTE, 1);
                    preyConfig.setTimeBlockAppUninstall(cal.getTimeInMillis() / 1000);
                }
            }
            abortBroadcast();
        }
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

        PreyLogger.d("Password onPasswordSucceeded");

        if (PreyConfig.getPreyConfig(context).isLockSet()){
            PreyLogger.d("Password was entered successfully");
            PreyConfig.getPreyConfig(context).setLock(false);
            PreyConfig.getPreyConfig(context).deleteUnlockPass();
            try{FroyoSupport.getInstance(context).changePasswordAndLock("", false);}catch(Exception e){}
            final Context ctx=context;
            new Thread(){
                public void run() {
                    String jobIdLock=PreyConfig.getPreyConfig(ctx).getJobIdLock();
                    String reason=null;
                    if(jobIdLock!=null&&!"".equals(jobIdLock)){
                        reason="{\"device_job_id\":\""+jobIdLock+"\"}";
                        PreyConfig.getPreyConfig(ctx).setJobIdLock("");
                    }
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","lock","stopped",reason));

                }
            }.start();
        }
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
    private boolean isPatternSet(Context ctx) {
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
    private boolean isPassOrPinSet(Context ctx) {
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
