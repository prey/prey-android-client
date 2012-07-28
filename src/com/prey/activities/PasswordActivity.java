package com.prey.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prey.PreyException;
import com.prey.R;
import com.prey.net.PreyWebServices;

public class PasswordActivity extends PreyActivity {
	
	int wrongPasswordIntents = 0;
	
	protected void bindPasswordControls() {
		Button checkPasswordOkButton = (Button) findViewById(R.id.password_btn_login);
		final EditText pass1 = ((EditText) findViewById(R.id.password_pass_txt));
		checkPasswordOkButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final String passwordtyped = pass1.getText().toString();
				if (passwordtyped.equals(""))
					Toast.makeText(PasswordActivity.this, R.string.preferences_password_length_error, Toast.LENGTH_LONG).show();
				else
					new CheckPassword().execute(passwordtyped);

			}
		});
		getPreyConfig().registerC2dm();
		
	}
	
	protected class CheckPassword extends AsyncTask<String, Void, Void> {

		ProgressDialog progressDialog = null;
		boolean isPasswordOk = false;
		boolean keepAsking = true;
		String error = null;
		

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(PasswordActivity.this);
			progressDialog.setMessage(PasswordActivity.this.getText(R.string.password_checking_dialog).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... password) {
			try {
				String email = getPreyConfig().getEmail();
				isPasswordOk = PreyWebServices.getInstance().checkPassword(PasswordActivity.this, email, password[0]);
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
				Toast.makeText(PasswordActivity.this, error, Toast.LENGTH_LONG).show();
			else if (!isPasswordOk) {
				boolean isAccountVerified = getPreyConfig().isAccountVerified();
				if (!isAccountVerified)
					Toast.makeText(PasswordActivity.this, R.string.verify_your_account_first, Toast.LENGTH_LONG).show();
				else {
					wrongPasswordIntents++;
					if (wrongPasswordIntents == 3) {
						Toast.makeText(PasswordActivity.this, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
						setResult(RESULT_CANCELED);
						finish();
					} else {
						Toast.makeText(PasswordActivity.this, R.string.password_wrong, Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				Intent intent = new Intent(PasswordActivity.this, PreyConfigurationActivity.class);
				startActivity(intent);
			}
		}
		
		

	}

}
