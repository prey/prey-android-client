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

import com.prey.PreyConfig;

public class UpdateSimPreference extends DialogPreference {

	private Context ctx;
	public UpdateSimPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			PreyConfig.getPreyConfig(ctx).saveSimInformation();
		}
	}

}
