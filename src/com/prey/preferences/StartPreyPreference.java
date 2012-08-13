/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.prey.PreyController;

public class StartPreyPreference extends DialogPreference {

	public StartPreyPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			// String url =
			// PreyWebServices.getInstance().getDeviceWebControlPanelUrl(getContext());
			// Intent fakeSMS = new Intent(getContext(),SmsReceiver.class);
			// getContext().sendBroadcast(fakeSMS);
			PreyController.startPrey(getContext());
		}
	}

}
