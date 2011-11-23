/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyException;
import com.prey.R;
import com.prey.activities.WelcomeActivity;
import com.prey.net.PreyWebServices;

public class DetachDevicePreferences extends DialogPreference {
	Context ctx = null;

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
			new DetachDevice().execute(null);
		}
	}

	private class DetachDevice extends AsyncTask<Void, Void, Void> {

		private String error = null;
		ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getContext());
			progressDialog.setMessage(getContext().getText(R.string.preferences_detach_dettaching_message).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... unused) {
			try {
				PreyConfig.getPreyConfig(getContext()).unregisterC2dm(false);
				PreyConfig.getPreyConfig(getContext()).setSecurityPrivilegesAlreadyPrompted(false);
				PreyWebServices.getInstance().deleteDevice(ctx);
				PreyConfig.getPreyConfig(getContext()).wipeData();

			} catch (PreyException e) {
				e.printStackTrace();
				error = e.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
			if (error != null) {
				Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
				showDialog(new Bundle());
			} else {
				Intent welcome = new Intent(getContext(), WelcomeActivity.class);
				welcome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getContext().startActivity(welcome);
			}
		}

	}
}
