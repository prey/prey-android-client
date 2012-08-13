/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.prey.PreyConfig;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.R;
public class DeviceAdminPreference extends DialogPreference {
	
	Context ctx = null;
	public DeviceAdminPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}
	
	@Override
	public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		if (preyConfig.isFroyoOrAbove()){
			if (FroyoSupport.getInstance(ctx).isAdminActive()){
				builder.setTitle(R.string.preferences_admin_enabled_dialog_title);
				builder.setMessage(R.string.preferences_admin_enabled_dialog_message);
			} else {
				builder.setTitle(R.string.preferences_admin_disabled_dialog_title);
				builder.setMessage(R.string.preferences_admin_disabled_dialog_message);
			}
		} 
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		
	
		if (which == DialogInterface.BUTTON_POSITIVE) {
			PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			if (preyConfig.isFroyoOrAbove()){
				FroyoSupport fSupport = FroyoSupport.getInstance(ctx);
				if (fSupport.isAdminActive()){
					fSupport.removeAdminPrivileges();
					setTitle(R.string.preferences_admin_disabled_title);
					setSummary(R.string.preferences_admin_disabled_summary);
				} else {
					Intent intent = fSupport.getAskForAdminPrivilegesIntent();
					ctx.startActivity(intent);
				}
			}
		}
	}

}
