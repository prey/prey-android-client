/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.AttributeSet;


import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.services.PreyDisablePowerOptionsService;

public class DisablePowerCheckBoxPreference extends CheckBoxPreference {
    public DisablePowerCheckBoxPreference(Context context) { super(context); }
    public DisablePowerCheckBoxPreference(Context context, AttributeSet attrs) { super(context, attrs); }
    public DisablePowerCheckBoxPreference(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle); }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        PreyLogger.i("DisablePowerCheckBoxPreference:" + checked);
        Context ctx=getContext();
        if(checked){
            notifyReady(ctx);
            ctx.startService(new Intent(ctx, PreyDisablePowerOptionsService.class));
        }else{
            notifyCancel(ctx);
            ctx.stopService(new Intent(ctx, PreyDisablePowerOptionsService.class));
        }
    }

    public static void notifyReady(Context ctx){
        if(PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove()) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx).setOngoing(true);
            notificationBuilder.setContentTitle(ctx.getString(R.string.disable_power_ready));
            notificationBuilder.setSmallIcon(R.drawable.status_bar);
            notificationBuilder.setPriority(Notification.PRIORITY_MIN);
            NotificationManager mgr= (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            mgr.notify(NOTIFY_ID, notificationBuilder.build());
        }
    }

    public static void notifyCancel(Context ctx){
        if(PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove()) {
            NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            mgr.cancel(NOTIFY_ID);
        }
    }

    public static int NOTIFY_ID=1337;

}
