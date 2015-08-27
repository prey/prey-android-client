/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.PreyStatus;
import com.prey.R;

public class PasswordActivity extends PreyActivity {

    int wrongPasswordIntents = 0;

    protected void bindPasswordControls() {
        Button checkPasswordOkButton = (Button) findViewById(R.id.password_btn_login);
        final EditText pass1 = ((EditText) findViewById(R.id.password_pass_txt));
        checkPasswordOkButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final String passwordtyped = pass1.getText().toString();
                final Context ctx = getApplicationContext();
                if (passwordtyped.equals(""))
                    Toast.makeText(ctx, R.string.preferences_password_length_error, Toast.LENGTH_LONG).show();
                else {
                    if (passwordtyped.length() < 6 || passwordtyped.length() > 32) {
                        Toast.makeText(ctx, ctx.getString(R.string.error_password_out_of_range, 6, 32), Toast.LENGTH_LONG).show();
                    } else {
                        new CheckPassword().execute(passwordtyped);
                    }
                }

            }
        });

        //Hack to fix hint's typeface: http://stackoverflow.com/questions/3406534/password-hint-font-in-android
        EditText password = (EditText) findViewById(R.id.password_pass_txt);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
    }

    protected void updateLoginScreen() {

    }

    protected class CheckPassword extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;
        boolean isPasswordOk = false;
        boolean keepAsking = true;
        String error = null;


        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(PasswordActivity.this);
                progressDialog.setMessage(PasswordActivity.this.getText(R.string.password_checking_dialog).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            } catch (Exception e) {

            }
        }

        @Override
        protected Void doInBackground(String... password) {
            try {
                String email = getPreyConfig().getEmail();
                isPasswordOk = PreyWebServices.getInstance().checkPassword(PasswordActivity.this, email, password[0]);


            } catch (PreyException e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
            }
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
                PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                startActivity(intent);
                new Thread(new EventManagerRunner(PasswordActivity.this, new Event(Event.APPLICATION_OPENED))).start();
            }
        }

    }

}

