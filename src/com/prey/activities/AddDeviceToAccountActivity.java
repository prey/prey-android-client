/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.prey.PreyAccountData;
import com.prey.R;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class AddDeviceToAccountActivity extends SetupActivity {

	private static final int NO_MORE_DEVICES_WARNING = 0;
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
		
		EditText password = (EditText) findViewById(R.id.add_device_pass);
		password.setTypeface(Typeface.DEFAULT);
		password.setTransformationMethod(new PasswordTransformationMethod());
		
		EditText email = (EditText) findViewById(R.id.add_device_email);
		email.setImeOptions(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

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
 
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		 AlertDialog ad=null;
		switch (id) {

		case ERROR:
			 ad = (AlertDialog) dialog;
			 ad.setIcon(R.drawable.error);
			 ad.setTitle(R.string.error_title);
			 ad.setMessage(error);
			 ad.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok), new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int id) {
				          //Handler code
				    }
				});
			 
			 ad.setCancelable(false);
			 
			 break;

		case NO_MORE_DEVICES_WARNING:
			 ad = (AlertDialog) dialog;
			ad.setIcon(R.drawable.info);
			ad.setTitle(R.string.set_old_user_no_more_devices_title);
			ad.setMessage(error);
			ad.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
			          //Handler code
			    }
			});
			ad.setCancelable(false) ;
			
			 break;
        default:
            super.onPrepareDialog(id, dialog);
		}
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
				error = null;
				PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceToAccount(AddDeviceToAccountActivity.this, data[0], data[1],
						data[2]);
				getPreyConfig().saveAccount(accountData);

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
					String message = getString(R.string.device_added_congratulations_text);
					Bundle bundle = new Bundle();
					bundle.putString("message", message);
					GoogleAnalyticsTracker.getInstance().trackEvent(
							"Device",  // Category
				            "Added",  // Action
				            "", // Label
				            1);
					Intent intent = new Intent(AddDeviceToAccountActivity.this, CongratulationsActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
				} else
					showDialog(ERROR);
			}
		}

	}

}
