/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import com.prey.R;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AgreementDialogActivity extends PreyActivity {

	protected static final int INSTRUCTIONS_SENT = 0;
	int wrongPasswordIntents = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agreement);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		TextView linkToTOS = (TextView) findViewById(R.id.linkToTosText);
		linkToTOS.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String url = "http://" + getPreyConfig().getPreyDomain() + "/terms";
				Intent internetIntent = new Intent(Intent.ACTION_VIEW);
				internetIntent.setData(Uri.parse(url));
				startActivity(internetIntent);

			}
		});

		Button checkPasswordOkButton = (Button) findViewById(R.id.agree_tos_button);
		checkPasswordOkButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();

			}
		});

		Button checkPasswordCancelButton = (Button) findViewById(R.id.dont_agree_tos_button);
		checkPasswordCancelButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}
}
/*
 * @Override protected Dialog onCreateDialog(int id) {
 * 
 * Dialog pass = null; switch (id) {
 * 
 * case INSTRUCTIONS_SENT: setResult(RESULT_FIRST_USER); String email =
 * PreyConfig.getPreyConfig(getApplicationContext()).getEmail(); String message
 * = getString(R.string.password_dialog_forgot_sent_label, email); //
 * label.setTextAppearance(getApplicationContext(),R.style.PreyTextAppearance);
 * pass = new
 * AlertDialog.Builder(AgreementDialogActivity.this).setIcon(R.drawable
 * .info).setTitle(R.string.password_dialog_forgot_sent_title)
 * .setMessage(message).setCancelable(true).setPositiveButton(R.string.ok, new
 * DialogInterface.OnClickListener() {
 * 
 * public void onClick(DialogInterface dialog, int which) {
 * 
 * } }).create(); } return pass; }
 * 
 * private class ReportForgotPassword extends AsyncTask<Void, Void, Void> {
 * 
 * ProgressDialog progressDialog = null; private String error = null;
 * 
 * @Override protected void onPreExecute() { progressDialog = new
 * ProgressDialog(AgreementDialogActivity.this);
 * progressDialog.setMessage(AgreementDialogActivity
 * .this.getText(R.string.password_dialog_forgot_requesting).toString());
 * progressDialog.setIndeterminate(true); progressDialog.setCancelable(false);
 * progressDialog.show(); }
 * 
 * @Override protected Void doInBackground(Void... passwords) { try {
 * PreyWebServices.getInstance().forgotPassword(AgreementDialogActivity.this); }
 * catch (PreyException e) { error = e.getMessage(); } return null; }
 * 
 * @Override protected void onPostExecute(Void unused) {
 * progressDialog.dismiss(); if (error == null) { showDialog(INSTRUCTIONS_SENT);
 * } else { Toast.makeText(AgreementDialogActivity.this, error,
 * Toast.LENGTH_LONG).show(); setResult(RESULT_CANCELED); }
 * 
 * }
 * 
 * }
 * 
 * private class CheckPassword extends AsyncTask<String, Void, Void> {
 * 
 * ProgressDialog progressDialog = null; boolean isPasswordOk = false; boolean
 * keepAsking = true; String error = null;
 * 
 * @Override protected void onPreExecute() {
 * 
 * progressDialog = new ProgressDialog(AgreementDialogActivity.this);
 * progressDialog.setMessage(AgreementDialogActivity.this.getText(R.string.
 * password_checking_dialog).toString()); progressDialog.setIndeterminate(true);
 * progressDialog.setCancelable(false); progressDialog.show(); }
 * 
 * @Override protected Void doInBackground(String... password) { try { String
 * email = PreyConfig.getPreyConfig(getBaseContext()).getEmail(); isPasswordOk =
 * PreyWebServices.getInstance().checkPassword(AgreementDialogActivity.this,
 * email, password[0]); if (isPasswordOk)
 * PreyConfig.getPreyConfig(AgreementDialogActivity
 * .this).setPassword(password[0]);
 * 
 * } catch (PreyException e) { error = e.getMessage(); } return null; }
 * 
 * @Override protected void onPostExecute(Void unused) {
 * progressDialog.dismiss(); if (error != null)
 * Toast.makeText(AgreementDialogActivity.this, error,
 * Toast.LENGTH_LONG).show(); else if (!isPasswordOk) { boolean
 * isAccountVerified =
 * PreyConfig.getPreyConfig(AgreementDialogActivity.this).isAccountVerified();
 * if (!isAccountVerified) Toast.makeText(AgreementDialogActivity.this,
 * R.string.verify_your_account_first, Toast.LENGTH_LONG).show(); else {
 * wrongPasswordIntents++; if (wrongPasswordIntents == 3) {
 * Toast.makeText(AgreementDialogActivity.this,
 * R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
 * setResult(RESULT_CANCELED); finish(); } else {
 * Toast.makeText(AgreementDialogActivity.this, R.string.password_wrong,
 * Toast.LENGTH_SHORT).show(); } } } else { setResult(RESULT_OK); finish(); } }
 * 
 * }
 * 
 * }
 */
