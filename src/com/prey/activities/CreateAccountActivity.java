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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyException;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.net.PreyWebServices;

public class CreateAccountActivity extends SetupActivity {

	private static final int CONFIRM_PASSWORD = 0;
	private static final int LOADING = 1;
	private static final int ERROR = 3;
	private String password = null;
	private String name = null;
	private String email = null;
	private String currentErrror = "";//

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_account);

		Button ok = (Button) findViewById(R.id.ButtonCreateAccount);
		ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				name = ((EditText) findViewById(R.id.set_new_user_dialog_name_edit)).getText().toString();
				email = ((EditText) findViewById(R.id.set_new_user_dialog_email_edit)).getText().toString();
				password = ((EditText) findViewById(R.id.set_new_user_dialog_password_edit)).getText().toString();

				if (name.equals("") || email.equals("") || password.equals("")) {
					Toast.makeText(CreateAccountActivity.this, R.string.error_all_fields_are_required, Toast.LENGTH_LONG).show();
				} else {
					showDialog(CONFIRM_PASSWORD);
				}
			}
		});

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);

		Dialog pass = null;
		switch (id) {

		case LOADING:
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getText(R.string.set_new_user_dialog_creating_popup).toString());
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			return dialog;

		case ERROR:
			return new AlertDialog.Builder(CreateAccountActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(currentErrror)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

						}
					}).setCancelable(false).create();

		case CONFIRM_PASSWORD:
			currentErrror = "";
			final View rePasswordView = factory.inflate(R.layout.check_password, null);
			TextView label = (TextView) rePasswordView.findViewById(R.id.password_dialog_label);
			label.setText(R.string.set_new_user_dialog_repassword);
			// label.setTextAppearance(getApplicationContext(),R.style.PreyTextAppearance);
			pass = new AlertDialog.Builder(CreateAccountActivity.this).setIcon(R.drawable.logo).setTitle(R.string.set_new_user_dialog_repassword_title)
					.setView(rePasswordView).setCancelable(true)
					.setPositiveButton(R.string.set_new_user_dialog_repassword_confirm, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							final String repassword = ((EditText) rePasswordView.findViewById(R.id.password)).getText().toString();
							if ((password != null) && (password.equals(repassword))) {
								showDialog(LOADING);
								new Thread(new Runnable() {
									public void run() {

										PreyAccountData accountData;
										try {
											accountData = PreyWebServices.getInstance().registerNewAccount(CreateAccountActivity.this, name, name, email,
													password, getDeviceType());
											PreyLogger.d( "Response creating account: " + accountData.toString());
											PreyConfig config = PreyConfig.getPreyConfig(CreateAccountActivity.this);
											config.saveAccount(accountData);
											dismissDialog(LOADING);
											setResult(RESULT_OK);
											finish();

										} catch (PreyException e) {
											dismissDialog(LOADING);
											final String msg = e.getMessage();
											runOnUiThread(new Runnable() {
												public void run() {
													currentErrror = msg;
													showDialog(ERROR);
												}
											});
										}
									}

								}).start();
							} else {
								dismissDialog(CONFIRM_PASSWORD);
								Toast.makeText(CreateAccountActivity.this, R.string.preferences_passwords_do_not_match, Toast.LENGTH_LONG).show();
							}
						}
					}).create();

		}
		return pass;
	}

}
