/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.os.Bundle;

import com.prey.R;

public class CheckPasswordActivity extends PasswordActivity {

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);
		bindPasswordControls();
		

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


}
