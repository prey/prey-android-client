/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.prey.R;
public class ChangePasswordPreferences extends DialogPreference {

	View changePassword = null;

	public ChangePasswordPreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ChangePasswordPreferences(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater i = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		changePassword = i.inflate(R.layout.set_password, null);
		return changePassword;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (changePassword != null && which == DialogInterface.BUTTON_POSITIVE) {

			final String old_password = ((EditText) changePassword.findViewById(R.id.old_password)).getText().toString();
			final String password = ((EditText) changePassword.findViewById(R.id.password)).getText().toString();
			final String repassword = ((EditText) changePassword.findViewById(R.id.password_confirm)).getText().toString();

			if (password.equals("")) {
				Toast.makeText(getContext(), R.string.preferences_password_length_error, Toast.LENGTH_LONG).show();
				showDialog(new Bundle());
			} else if (password.equals(repassword)) {
				new ChangePassword().execute(old_password,password);
			} else {
				Toast.makeText(getContext(), R.string.preferences_passwords_do_not_match, Toast.LENGTH_LONG).show();
				showDialog(new Bundle());
			}
		}
	}

	private class ChangePassword extends AsyncTask<String, Void, Void> {

		private String error = null;
		ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getContext());
			progressDialog.setMessage(getContext().getText(R.string.preferences_passwords_updating_dialog).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... passwords) {
			/*try {
				String email = PreyConfig.getPreyConfig(getContext()).getEmail();
				PreyWebServices.getInstance().changePassword(getContext(), email,passwords[0], passwords[1]);

			} catch (PreyException e) {
				e.printStackTrace();
				error = e.getMessage();
			}*/
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
			if (error == null) {
				Toast.makeText(getContext(), R.string.preferences_passwords_successfully_changed, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
				showDialog(new Bundle());
			}
		}

	}
}
