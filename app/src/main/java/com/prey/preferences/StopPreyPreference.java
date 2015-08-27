/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;


import com.prey.PreyLogger;

public class StopPreyPreference extends DialogPreference {

    public StopPreyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        PreyLogger.d("Stopping Prey");
        /*if (which == DialogInterface.BUTTON_POSITIVE) {
			PreyController.stopPrey(getContext());
		}*/
    }

}
