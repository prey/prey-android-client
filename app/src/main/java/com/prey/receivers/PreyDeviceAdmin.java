/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

import com.prey.R;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class PreyDeviceAdmin extends DeviceAdminReceiver {

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

}
