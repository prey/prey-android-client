/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prey.PreyException;
import com.prey.R;
import com.prey.net.PreyWebServices;

public class CheckPasswordActivity extends PreyActivity {

	protected static final int INSTRUCTIONS_SENT = 0;

	private static final int START_PREFERENCES = 100;
	int wrongPasswordIntents = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);
		Button checkPasswordOkButton = (Button) findViewById(R.id.password_btn_login);
		final EditText pass1 = ((EditText) findViewById(R.id.password_pass_txt));
		checkPasswordOkButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final String passwordtyped = pass1.getText().toString();
				if (passwordtyped.equals(""))
					Toast.makeText(CheckPasswordActivity.this, R.string.preferences_password_length_error, Toast.LENGTH_LONG).show();
				else
					new CheckPassword().execute(passwordtyped);

			}
		});
		getPreyConfig().registerC2dm();
		

		/*
		if (PreyConfig.getPreyConfig(getApplicationContext()).askForPassword()) {
			Intent intent = new Intent(WelcomeActivity.this, CheckPasswordActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivityForResult(intent, CHECKING_PASSWORD);
		} else
			goToPreferences();
		 */
/*
		TextView forgotPasswordText = (TextView) findViewById(R.id.check_password_forgot_text2);
		forgotPasswordText.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				//new ReportForgotPassword().execute();
				Intent forgotIntent = new Intent(Intent.ACTION_VIEW);
				forgotIntent.setData(Uri.parse(PreyConfig.getPreyConfig(getApplicationContext()).getPreyUrl().concat("forgot")));
				startActivity(forgotIntent);

			}
		});
*/
	}
	
	

	
	


	/*
	@Override
	protected Dialog onCreateDialog(int id) {

		Dialog pass = null;
		switch (id) {

		case INSTRUCTIONS_SENT:
			setResult(RESULT_FIRST_USER);
			String email = PreyConfig.getPreyConfig(getApplicationContext()).getEmail();
			String message = getString(R.string.password_dialog_forgot_sent_label, email);
			// label.setTextAppearance(getApplicationContext(),R.style.PreyTextAppearance);
			pass = new AlertDialog.Builder(CheckPasswordActivity.this).setIcon(R.drawable.info).setTitle(R.string.password_dialog_forgot_sent_title)
					.setMessage(message).setCancelable(true).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

						}
					}).create();
		}
		return pass;
	}

	private class ReportForgotPassword extends AsyncTask<Void, Void, Void> {

		ProgressDialog progressDialog = null;
		private String error = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(CheckPasswordActivity.this);
			progressDialog.setMessage(CheckPasswordActivity.this.getText(R.string.password_dialog_forgot_requesting).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... passwords) {
			try {
				PreyWebServices.getInstance().forgotPassword(CheckPasswordActivity.this);
			} catch (PreyException e) {
				error = e.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
			if (error == null) {
				showDialog(INSTRUCTIONS_SENT);
			} else {
				Toast.makeText(CheckPasswordActivity.this, error, Toast.LENGTH_LONG).show();
				setResult(RESULT_CANCELED);
			}

		}

	}
*/
	private class CheckPassword extends AsyncTask<String, Void, Void> {

		ProgressDialog progressDialog = null;
		boolean isPasswordOk = false;
		boolean keepAsking = true;
		String error = null;
		

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(CheckPasswordActivity.this);
			progressDialog.setMessage(CheckPasswordActivity.this.getText(R.string.password_checking_dialog).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... password) {
			try {
				String email = getPreyConfig().getEmail();
				isPasswordOk = PreyWebServices.getInstance().checkPassword(CheckPasswordActivity.this, email, password[0]);
				//if (isPasswordOk)
					//PreyConfig.getPreyConfig(CheckPasswordActivity.this).setPassword(password[0]);

			} catch (PreyException e) {
				error = e.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			if (progressDialog.isShowing())
				progressDialog.dismiss();
			if (error != null)
				Toast.makeText(CheckPasswordActivity.this, error, Toast.LENGTH_LONG).show();
			else if (!isPasswordOk) {
				boolean isAccountVerified = getPreyConfig().isAccountVerified();
				if (!isAccountVerified)
					Toast.makeText(CheckPasswordActivity.this, R.string.verify_your_account_first, Toast.LENGTH_LONG).show();
				else {
					wrongPasswordIntents++;
					if (wrongPasswordIntents == 3) {
						Toast.makeText(CheckPasswordActivity.this, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
						setResult(RESULT_CANCELED);
						finish();
					} else {
						Toast.makeText(CheckPasswordActivity.this, R.string.password_wrong, Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				Intent intent = new Intent(CheckPasswordActivity.this, PreyConfigurationActivity.class);
				startActivity(intent);
			}
		}
		
		

	}

}
