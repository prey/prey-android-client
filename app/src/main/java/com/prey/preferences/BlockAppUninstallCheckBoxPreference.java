/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

public class BlockAppUninstallCheckBoxPreference extends CheckBoxPreference {

    public BlockAppUninstallCheckBoxPreference(Context context) {
        super(context);
    }

    public BlockAppUninstallCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlockAppUninstallCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        PreyLogger.d("LOCK BlockAppUninstallCheckBoxPreference:" + checked);
        PreyConfig.getPreyConfig(getContext()).setBlockAppUninstall(checked);
    }

}
