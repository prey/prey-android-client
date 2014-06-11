/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

public class GoToControlPanelDialogPreference extends DialogPreference {

	public GoToControlPanelDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public GoToControlPanelDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		 
	 			// String url =
			// PreyWebServices.getInstance().getDeviceWebControlPanelUrl(getContext());
			String url = PreyConfig.getPreyConfig(getContext()).getPreyUrl();
			PreyLogger.d("url control:"+url);
			Intent internetIntent = new Intent(Intent.ACTION_VIEW);
			internetIntent.setData(Uri.parse(url));
			getContext().startActivity(internetIntent);
		 
	}

	
	 
}
