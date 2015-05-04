/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.prey.R;
import com.prey.activities.LoginActivity;
import com.prey.services.DetachDeviceService;
public class DetachDevicePreferences extends DialogPreference {
	public static final String DETACHDEVICE_FILTER = "DetachDevicePreferences_RECEIVER";

	Context ctx = null;
	private DetachDeviceReceiver receiver;

	public DetachDevicePreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	public DetachDevicePreferences(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.ctx = context;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			receiver = new DetachDeviceReceiver();
			ctx.registerReceiver(receiver, new IntentFilter(DETACHDEVICE_FILTER));
			Intent detachDevice = new Intent(ctx, DetachDeviceService.class);
			receiver.showProgressDialog();
			ctx.startService(detachDevice);
		}
	}

	@Override
	public void onActivityDestroy() {
		super.onActivityDestroy();
		if (receiver != null) {
			ctx.unregisterReceiver(receiver);
		}
	}

	public class DetachDeviceReceiver extends BroadcastReceiver {

		private String error = null;
		ProgressDialog progressDialog = null;

		public void showProgressDialog() {
			progressDialog = new ProgressDialog(getContext());
			progressDialog.setMessage(getContext().getText(R.string.preferences_detach_dettaching_message).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		public void onReceive(Context receiverContext, Intent receiverIntent) {
			error = receiverIntent.getStringExtra("error");
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			if (error != null) {
				Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
				showDialog(new Bundle());
			} else {
				Intent welcome = new Intent(getContext(), LoginActivity.class);
				getContext().startActivity(welcome);
			}
		}

	}
}
