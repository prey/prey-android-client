package com.prey.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyException;
import com.prey.R;
import com.prey.net.PreyWebServices;

public class CheckPasswordActivity extends Activity {

	protected static final int INSTRUCTIONS_SENT = 0;
	int wrongPasswordIntents = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_password2);

		TextView forgotPasswordText = (TextView) findViewById(R.id.check_password_forgot_text2);
		forgotPasswordText.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				//new ReportForgotPassword().execute();
				Intent forgotIntent = new Intent(Intent.ACTION_VIEW);
				forgotIntent.setData(Uri.parse(PreyConfig.getPreyConfig(getApplicationContext()).getPreyUrl().concat("forgot")));
				startActivity(forgotIntent);

			}
		});

		Button checkPasswordOkButton = (Button) findViewById(R.id.check_password_ok_button);
		checkPasswordOkButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				EditText pass1 = ((EditText) findViewById(R.id.check_password_edittext));
				final String passwordtyped = pass1.getText().toString();
				if (passwordtyped.equals(""))
					Toast.makeText(CheckPasswordActivity.this, R.string.preferences_password_length_error, Toast.LENGTH_LONG).show();
				else
					new CheckPassword().execute(passwordtyped);

			}
		});

		Button checkPasswordCancelButton = (Button) findViewById(R.id.check_password_cancel_button);
		checkPasswordCancelButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

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
				String email = PreyConfig.getPreyConfig(getBaseContext()).getEmail();
				isPasswordOk = PreyWebServices.getInstance().checkPassword(CheckPasswordActivity.this, email, password[0]);
				if (isPasswordOk)
					PreyConfig.getPreyConfig(CheckPasswordActivity.this).setPassword(password[0]);

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
				boolean isAccountVerified = PreyConfig.getPreyConfig(CheckPasswordActivity.this).isAccountVerified();
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
				setResult(RESULT_OK);
				finish();
			}
		}

	}

}
