package com.prey.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.backwardcompatibility.FroyoSupport;

public class WelcomeActivity extends Activity {

	private static final int CREATE_ACCOUNT = 0;
	private static final int WELCOME_DIALOG = 1;
	private static final int ADD_THIS_DEVICE = 2;
	private static final int AGREEMENTS = 3;
	private static final int LOADING = 4;
	private static final int LOADING_ADD_DEVICE = 5;
	private static final int ERROR = 6;
	// private static final int CONFIRM_PASSWORD = 7;
	private static final int CHECKING_PASSWORD = 8;
	private static final int CONGRATULATIONS_NEW_ACCOUNT = 9;
	private static final int SECURITY_PRIVILEGES = 10;

	private static final int START_PREFERENCES = 100;

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
		/*
		 * if (!isPreyAgreementRead()){ Intent intent = new
		 * Intent(WelcomeActivity.this, AgreementDialogActivity.class);
		 * startActivityForResult(intent, AGREEMENTS); } else
		 */
		startup();
	}
	


	private void startup() {
		
		if (!isThisDeviceAlreadyRegisteredWithPrey()) {
			showDialog(WELCOME_DIALOG);
		} else {
			PreyConfig.getPreyConfig(this).registerC2dm();
			// First delete notifications (in case Activity was started by one
			// of them)
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(R.string.preyForAndroid_name);

			if (PreyConfig.getPreyConfig(getApplicationContext()).askForPassword()) {
				Intent intent = new Intent(WelcomeActivity.this, CheckPasswordActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivityForResult(intent, CHECKING_PASSWORD);
			} else
				goToPreferences();
		}
	}

	/**
	 * This is called after an account creation, or when closing the preferences
	 * screen. When closing preferences, we are closing the application as well,
	 * except when the application doesn't have an account associated. In this
	 * case, we are showing the welcome dialog again (this case could happen
	 * after detaching the device).
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CREATE_ACCOUNT) {
			if (resultCode == RESULT_OK) {
				showDialog(CONGRATULATIONS_NEW_ACCOUNT);
			} else
				showDialog(WELCOME_DIALOG);
		} else if (requestCode == START_PREFERENCES) {
			if (resultCode == RESULT_FIRST_USER)
				showDialog(WELCOME_DIALOG);
			else
				finish();
		} else if (requestCode == CHECKING_PASSWORD) {
			if (resultCode == RESULT_OK)
				goToPreferences();
			else
				finish();
		} else if (requestCode == ADD_THIS_DEVICE) {
			if (resultCode == RESULT_OK){
				PreyConfig.getPreyConfig(this).registerC2dm();
				goToPreferences();
			}
			else
				showDialog(WELCOME_DIALOG);
		} else if (requestCode == SECURITY_PRIVILEGES) {
				goToPreferences();
		} else if (requestCode == AGREEMENTS) {
			if (resultCode == RESULT_OK)
				startup();
			else
				finish();
		} else
			finish();
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

		case WELCOME_DIALOG:
			pass = new AlertDialog.Builder(WelcomeActivity.this).setIcon(R.drawable.logo).setTitle(R.string.first_dialog_title)
					.setMessage(R.string.first_dialog_message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dismissDialog(WELCOME_DIALOG);
							Intent intent = new Intent(WelcomeActivity.this, AddDeviceToAccountActivity.class);
							startActivityForResult(intent, ADD_THIS_DEVICE);
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dismissDialog(WELCOME_DIALOG);
							// showDialog(CREATE_ACCOUNT);
							Intent intent = new Intent(WelcomeActivity.this, CreateAccountActivity.class);
							startActivityForResult(intent, CREATE_ACCOUNT);
						}
					}).setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {

						public void onCancel(DialogInterface dialog) {
							finish();

						}
					}).create();
			return pass;

		case CONGRATULATIONS_NEW_ACCOUNT:
			String mail = PreyConfig.getPreyConfig(WelcomeActivity.this).getEmail();
			String message = getString(R.string.new_account_congratulations_text, mail);
			return new AlertDialog.Builder(WelcomeActivity.this).setIcon(R.drawable.logo).setTitle(R.string.congratulations_title).setMessage(message)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							dismissDialog(CONGRATULATIONS_NEW_ACCOUNT);
							goToCheckPassword();
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

	protected void goToPreferences() {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		if (preyConfig.isFroyoOrAbove() && !preyConfig.isSecurityPrivilegesAlreadyPrompted()){
			Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
			startActivityForResult(intent,SECURITY_PRIVILEGES);
		}
		else {
			PreyLogger.d("Starting preferences page");
			Intent preferences = new Intent(getApplicationContext(), PreyConfigurationActivity.class);
			// preferences.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
			// Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(preferences, START_PREFERENCES);
		}
	}

	protected void goToCheckPassword() {
		Intent preferences = new Intent(WelcomeActivity.this, CheckPasswordActivity.class);
		preferences.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(preferences, CHECKING_PASSWORD);
	}

	private boolean isThisDeviceAlreadyRegisteredWithPrey() {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(WelcomeActivity.this);
		return preyConfig.isThisDeviceAlreadyRegisteredWithPrey(false);
	}
	
	

}
