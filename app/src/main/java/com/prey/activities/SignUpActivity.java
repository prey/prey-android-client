/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.util.KeyboardStatusDetector;
import com.prey.util.KeyboardVisibilityListener;

public class SignUpActivity extends Activity {

    private static final int ERROR = 1;
    private String error = null;
    private String email = null;

    public void onResume() {
        PreyLogger.i("onResume of SignUpActivity");
        super.onResume();

    }

    @Override
    public void onPause() {
        PreyLogger.i("onPause of SignUpActivity");
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setContentView(R.layout.signup);
        PreyLogger.i("onCreate of SignUpActivity");

        final EditText nameText=((EditText)findViewById(R.id.editTextName));
        final EditText emailText=((EditText)findViewById(R.id.editTextEmailAddress));
        final EditText passwordText=((EditText)findViewById(R.id.editTextPassword));
        Button buttonSignup = (Button) findViewById(R.id.buttonSignup);

        final TextView linkSignup = (TextView) findViewById(R.id.linkSignup);



        Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");

        TextView textViewInit1=(TextView)findViewById(R.id.textViewInit1);
        TextView textViewInit2=(TextView)findViewById(R.id.textViewInit2);
        textViewInit1.setTypeface(magdacleanmonoRegular);
        textViewInit2.setTypeface(titilliumWebBold);



        linkSignup.setTypeface(titilliumWebBold);
        buttonSignup.setTypeface(titilliumWebBold);

        nameText.setTypeface(magdacleanmonoRegular);
        emailText.setTypeface(magdacleanmonoRegular);
        passwordText.setTypeface(magdacleanmonoRegular);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);



        final int halfHeight=metrics.heightPixels/3;
        KeyboardStatusDetector keyboard = new KeyboardStatusDetector();

        keyboard.registerActivity(this); // or register to an activity
        keyboard.setVisibilityListener(new KeyboardVisibilityListener() {

            @Override
            public void onVisibilityChanged(boolean keyboardVisible) {
                try {


                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linkSignup.getLayoutParams();
                    if (keyboardVisible) {
                        PreyLogger.d("key on");

                        params.setMargins(20, 0, 20, halfHeight);
                    } else {
                        PreyLogger.d("key off");

                        params.setMargins(20, 0, 20, 20);
                    }
                    linkSignup.setLayoutParams(params);
                } catch (Exception e) {
                    PreyLogger.i("error:" + e.getMessage());
                }
            }
        });


        buttonSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString();
                email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                Context ctx = getApplicationContext();
                if (email == null || email.equals("") || password == null || password.equals("")) {
                    Toast.makeText(ctx, R.string.error_all_fields_are_required, Toast.LENGTH_LONG).show();
                } else {
                    if (email.length() < 6 || email.length() > 100) {
                        Toast.makeText(ctx, ctx.getString(R.string.error_mail_out_of_range, "6", "100"), Toast.LENGTH_LONG).show();
                    } else {
                        if (password.length() < 6 || password.length() > 32) {
                            Toast.makeText(ctx, ctx.getString(R.string.error_password_out_of_range, "6", "32"), Toast.LENGTH_LONG).show();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                new CreateAccount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, name, email, password);
                            else
                                new CreateAccount().execute(name, email, password);
                        }
                    }
                }

            }
        });


        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), InitActivity.class);
        startActivity(intent);
        finish();
    }


    private class CreateAccount extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SignUpActivity.this);
            progressDialog.setMessage(SignUpActivity.this.getText(R.string.creating_account_please_wait).toString());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                error = null;
                PreyAccountData accountData = PreyWebServices.getInstance().registerNewAccount(getApplicationContext(), data[0], data[1], data[2], PreyUtils.getDeviceType(getApplication()));
                PreyLogger.d("Response creating account: " + accountData.toString());
                PreyConfig.getPreyConfig(getApplicationContext()).saveAccount(accountData);
                PreyConfig.getPreyConfig(getApplicationContext()).registerC2dm();
                PreyWebServices.getInstance().sendEvent(getApplication(),PreyConfig.ANDROID_SIGN_UP);
            } catch (PreyException e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
            if (error == null) {
                String message = getString(R.string.new_account_congratulations_text, email);
                Bundle bundle = new Bundle();
                bundle.putString("message", message);
                Intent intent =null;
                if (PreyConfig.getPreyConfig(SignUpActivity.this).isChromebook()) {
                    intent = new Intent(SignUpActivity.this, WelcomeActivity.class);
                    PreyConfig.getPreyConfig(SignUpActivity.this).setProtectReady(true);
                }else {
                    intent = new Intent(SignUpActivity.this, PermissionInformationActivity.class);
                }
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            } else
                showDialog(ERROR);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog pass = null;
        switch (id) {

            case ERROR:
                return new AlertDialog.Builder(SignUpActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).create();
        }
        return pass;
    }
}
