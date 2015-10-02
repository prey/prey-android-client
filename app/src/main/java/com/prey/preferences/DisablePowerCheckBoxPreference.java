/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;


import com.prey.PreyConfig;
import com.prey.PreyLogger;
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
            PreyConfig.getPreyConfig(ctx).setDisablePowerOptions(checked);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            boolean disablePowerOptions = settings.getBoolean(PreyConfig.PREFS_DISABLE_POWER_OPTIONS, false);
            if(disablePowerOptions){
                getContext().startService(new Intent(ctx, PreyDisablePowerOptionsService.class));
            }else{
                getContext().stopService(new Intent(ctx, PreyDisablePowerOptionsService.class));
            }

    }
}
