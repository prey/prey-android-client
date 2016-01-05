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

/**
 * Created by oso on 04-01-16.
 */
public class SignalFlareCheckBoxPreference extends CheckBoxPreference {
    public SignalFlareCheckBoxPreference(Context context) { super(context); }
    public SignalFlareCheckBoxPreference(Context context, AttributeSet attrs) { super(context, attrs); }
    public SignalFlareCheckBoxPreference(Context context, AttributeSet attrs,
                                         int defStyle) {
        super(context, attrs, defStyle); }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        PreyLogger.i("SignalFlareCheckBoxPreference:" + checked);
        Context ctx=getContext();
        PreyConfig.getPreyConfig(ctx).setSignalFlare(checked);
        if(checked){
            PreyConfig.getPreyConfig(ctx).setSignalFlareDate(0);
        }
    }
}
