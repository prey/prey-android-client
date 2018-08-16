/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.AttributeSet;


import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.services.PreyDisablePowerOptionsService;
import com.prey.services.PreyNotificationForeGroundService;

public class DisablePowerCheckBoxPreference extends CheckBoxPreference {
    public DisablePowerCheckBoxPreference(Context context) { super(context); }
    public DisablePowerCheckBoxPreference(Context context, AttributeSet attrs) { super(context, attrs); }
    public DisablePowerCheckBoxPreference(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle); }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        PreyLogger.d("DisablePowerCheckBoxPreference:" + checked);
        Context ctx=getContext();
        if(checked){
            notifyReady(ctx);
            ctx.startService(new Intent(ctx, PreyDisablePowerOptionsService.class));
        }else{
            notifyCancel(ctx);
            ctx.stopService(new Intent(ctx, PreyDisablePowerOptionsService.class));
        }
        PreyConfig.getPreyConfig(ctx).setDisablePowerOptions(checked);
    }

    public static void notifyReady(Context ctx){
        try{
            ctx.startService(new Intent(ctx, PreyNotificationForeGroundService.class));
        }catch(Exception e){}
    }

    public static void notifyCancel(Context ctx){
        try{
            ctx.stopService(new Intent(ctx, PreyNotificationForeGroundService.class));
        }catch(Exception e){}
    }



    public static int NOTIFY_ID=1337;

}
