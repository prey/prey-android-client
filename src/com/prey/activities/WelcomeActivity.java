/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
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
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.prey.PreyConfig;
import com.prey.R;

public class WelcomeActivity extends PreyActivity {

	private static final int CREATE_ACCOUNT = 0;
	private static final int ADD_THIS_DEVICE = 2;
	private static final int LOADING = 4;
	private static final int LOADING_ADD_DEVICE = 5;
	private static final int ERROR = 6;
	// private static final int CONFIRM_PASSWORD = 7;
	private static final int CONGRATULATIONS_NEW_ACCOUNT = 9;


	int wrongPasswordIntents = 0;
	boolean isPasswordOk = false;
	boolean wasCancelled = false;
	boolean emptyFields = false;
	int nextDialog;
	String currentErrror = "";

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		Button newUser = (Button) findViewById(R.id.btn_welcome_newuser);
		Button oldUser = (Button) findViewById(R.id.btn_welcome_olduser);
		
		newUser.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, CreateAccountActivity.class);
				startActivityForResult(intent, CREATE_ACCOUNT);
			}
		});
		
		oldUser.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, AddDeviceToAccountActivity.class);
				startActivityForResult(intent, ADD_THIS_DEVICE);
			}
		});
	}


	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {

		switch (id) {
		case ERROR:
			((AlertDialog) dialog).setMessage(currentErrror);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog pass = null;
		switch (id) {

		case CONGRATULATIONS_NEW_ACCOUNT:
			String mail = getPreyConfig().getEmail();
			String message = getString(R.string.new_account_congratulations_text, mail);
			return new AlertDialog.Builder(WelcomeActivity.this).setIcon(R.drawable.logo).setTitle(R.string.congratulations_title).setMessage(message)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							dismissDialog(CONGRATULATIONS_NEW_ACCOUNT);
							//goToCheckPassword();
						}
					}).setCancelable(false).create();

		case ERROR:
			return new AlertDialog.Builder(WelcomeActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(currentErrror)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							showDialog(nextDialog);

						}
					}).setCancelable(false).create();

		case LOADING:
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getText(R.string.set_new_user_dialog_creating_popup).toString());
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			return dialog;

		case LOADING_ADD_DEVICE:
			ProgressDialog dialog_adding_device = new ProgressDialog(this);
			dialog_adding_device.setMessage(getText(R.string.set_old_user_loading).toString());
			dialog_adding_device.setIndeterminate(true);
			dialog_adding_device.setCancelable(false);
			return dialog_adding_device;

		}
		return pass;

	}

}
