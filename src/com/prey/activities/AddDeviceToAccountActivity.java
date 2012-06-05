/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prey.NoMoreDevicesAllowedException;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyException;
import com.prey.R;
import com.prey.net.PreyWebServices;

public class AddDeviceToAccountActivity extends SetupActivity {

	private static final int NO_MORE_DEVICES_WARNING = 0;
	private static final int CONGRATULATIONS_DEVICE_ADDED = 1;
	private static final int ERROR = 3;
	private String error = null;
	private boolean noMoreDeviceError = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_device);

		Button ok = (Button) findViewById(R.id.add_device_btn_ok);
		ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				error = null;
				final String email = ((EditText) findViewById(R.id.add_device_email)).getText().toString();
				final String password = ((EditText) findViewById(R.id.add_device_pass)).getText().toString();

				if (email.equals("") || password.equals("")) {
					Toast.makeText(AddDeviceToAccountActivity.this, R.string.error_all_fields_are_required, Toast.LENGTH_LONG).show();
				} else {
					new AddDeviceToAccount().execute(email, password, getDeviceType());
				}
			}
		});

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog pass = null;
		switch (id) {

		case ERROR:
			return new AlertDialog.Builder(AddDeviceToAccountActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					}).setCancelable(false).create();

		case NO_MORE_DEVICES_WARNING:
			return new AlertDialog.Builder(AddDeviceToAccountActivity.this).setIcon(R.drawable.info).setTitle(R.string.set_old_user_no_more_devices_title)
					.setMessage(error).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
						}
					}).setCancelable(false).create();
		}
		return pass;
	}

	private class AddDeviceToAccount extends AsyncTask<String, Void, Void> {

		ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(AddDeviceToAccountActivity.this);
			progressDialog.setMessage(AddDeviceToAccountActivity.this.getText(R.string.set_old_user_loading).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... data) {
			try {
				noMoreDeviceError = false;
				PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceToAccount(AddDeviceToAccountActivity.this, data[0], data[1],
						data[2]);
				PreyConfig config = PreyConfig.getPreyConfig(AddDeviceToAccountActivity.this);
				config.saveAccount(accountData);

			} catch (PreyException e) {
				error = e.getMessage();
				try {
					NoMoreDevicesAllowedException noMoreDevices = (NoMoreDevicesAllowedException) e;
					noMoreDeviceError = true;

				} catch (ClassCastException e1) {
					noMoreDeviceError = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
			if (noMoreDeviceError)
				showDialog(NO_MORE_DEVICES_WARNING);

			else {
				if (error == null) {
					Intent intent = new Intent(AddDeviceToAccountActivity.this, CongratulationsActivity.class);
					startActivity(intent);
				} else
					showDialog(ERROR);
			}
		}

	}

}
