/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.services.PreyNotificationForeGroundService;

public class RunBackgroundCheckBoxPreference extends CheckBoxPreference {

    public RunBackgroundCheckBoxPreference(Context context) {
        super(context);
    }

    public RunBackgroundCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RunBackgroundCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        PreyLogger.d("RunBackgroundCheckBoxPreference:" + checked);
        Context ctx=getContext();
        if(checked){
            notifyReady(ctx);
        }else{
            notifyCancel(ctx);
        }
        PreyConfig.getPreyConfig(ctx).setRunBackground(checked);
    }

    public static void notifyReady(Context ctx){
    }

    public static void notifyCancel(Context ctx){
        if(!PreyUtils.isChromebook(ctx)) {
            try {
                ctx.stopService(new Intent(ctx, PreyNotificationForeGroundService.class));
            } catch (Exception e) {
                PreyLogger.e("notifyCancel:" + e.getMessage(), e);
            }
        }
    }

}