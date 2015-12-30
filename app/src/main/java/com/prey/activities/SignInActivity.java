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
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.util.KeyboardStatusDetector;
import com.prey.util.KeyboardVisibilityListener;

public class SignInActivity extends Activity {


    private static final int NO_MORE_DEVICES_WARNING = 0;
    private static final int ERROR = 3;
    private String error = null;
    private boolean noMoreDeviceError = false;


    @Override
    public void onResume() {
        PreyLogger.i("onResume of SignInActivity");
        super.onResume();

    }

    @Override
    public void onPause() {
        PreyLogger.i("onPause of SignInActivity");
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
        this.setContentView(R.layout.signin);
        PreyLogger.i("onCreate of SignInActivity");

        Button buttonSignin = (Button) findViewById(R.id.buttonSignin);


        final EditText emailText=((EditText)findViewById(R.id.editTextEmailAddress));
        final EditText passwordText=((EditText)findViewById(R.id.editTextPassword));


        final Context ctx = this;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);



        final int halfHeight=metrics.heightPixels/3;
        final TextView linkSignin=(TextView)findViewById(R.id.linkSignin);


        Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");

        TextView textViewInit1=(TextView)findViewById(R.id.textViewInit1);
        TextView textViewInit2=(TextView)findViewById(R.id.textViewInit2);
        EditText editTextEmailAddress=(EditText)findViewById(R.id.editTextEmailAddress);
        EditText editTextPassword=(EditText)findViewById(R.id.editTextPassword);

        textViewInit1.setTypeface(magdacleanmonoRegular);
        textViewInit2.setTypeface(titilliumWebBold);
        buttonSignin.setTypeface(titilliumWebBold);

        linkSignin.setTypeface(titilliumWebBold);
        editTextEmailAddress.setTypeface(magdacleanmonoRegular);
        editTextPassword.setTypeface(magdacleanmonoRegular);


        KeyboardStatusDetector keyboard = new KeyboardStatusDetector();

        keyboard.registerActivity(this); // or register to an activity
        keyboard.setVisibilityListener(new KeyboardVisibilityListener() {

            @Override
            public void onVisibilityChanged(boolean keyboardVisible) {
                try {


                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)linkSignin.getLayoutParams();
                    if (keyboardVisible) {
                        PreyLogger.d("key on");

                        params.setMargins(20,0,20,halfHeight);
                    } else {
                        PreyLogger.d("key off");

                        params.setMargins(20,0,20,20);
                    }
                    linkSignin.setLayoutParams(params);
                } catch (Exception e) {
                    PreyLogger.i("error:"+e.getMessage());
                }
            }
        });




        buttonSignin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();


                if (email == null || email.equals("") || password == null || password.equals("")) {
                    Toast.makeText(ctx, R.string.error_all_fields_are_required, Toast.LENGTH_LONG).show();
                } else {
                    if (email.length() < 6 || email.length() > 100) {
                        Toast.makeText(ctx, ctx.getString(R.string.error_mail_out_of_range, 6, 100), Toast.LENGTH_LONG).show();
                    } else {
                        if (password.length() < 6 || password.length() > 32) {
                            Toast.makeText(ctx, ctx.getString(R.string.error_password_out_of_range, 6, 32), Toast.LENGTH_LONG).show();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                new AddDeviceToAccount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, email, password, PreyUtils.getDeviceType(ctx));
                            else
                                new AddDeviceToAccount().execute(email, password, PreyUtils.getDeviceType(ctx));
                        }
                    }
                }


            }
        });


        linkSignin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
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


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog pass = null;
        switch (id) {

            case ERROR:
                return new AlertDialog.Builder(SignInActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).create();

            case NO_MORE_DEVICES_WARNING:
                return new AlertDialog.Builder(SignInActivity.this).setIcon(R.drawable.info).setTitle(R.string.set_old_user_no_more_devices_title).setMessage(error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).create();
        }
        return pass;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        AlertDialog ad = null;
        switch (id) {

            case ERROR:
                ad = (AlertDialog) dialog;
                ad.setIcon(R.drawable.error);
                ad.setTitle(R.string.error_title);
                ad.setMessage(error);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handler code
                    }
                });

                ad.setCancelable(false);

                break;

            case NO_MORE_DEVICES_WARNING:
                ad = (AlertDialog) dialog;
                ad.setIcon(R.drawable.info);
                ad.setTitle(R.string.set_old_user_no_more_devices_title);
                ad.setMessage(error);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handler code
                    }
                });
                ad.setCancelable(false);

                break;
            default:
                super.onPrepareDialog(id, dialog);
        }
    }
    private class AddDeviceToAccount extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SignInActivity.this);
            progressDialog.setMessage(SignInActivity.this.getText(R.string.set_old_user_loading).toString());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                noMoreDeviceError = false;
                error = null;
                PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceToAccount(SignInActivity.this, data[0], data[1], data[2]);
                PreyConfig.getPreyConfig(getApplicationContext()).saveAccount(accountData);
                PreyConfig.getPreyConfig(getApplication()).registerC2dm();
                PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_SIGN_IN);
            } catch (PreyException e) {
                error = e.getMessage();
                try {
                    NoMoreDevicesAllowedException noMoreDevices = (NoMoreDevicesAllowedException) e;
                    noMoreDeviceError = true;

                } catch (ClassCastException e1) {
                    noMoreDeviceError = false;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
            if (noMoreDeviceError)
                showDialog(NO_MORE_DEVICES_WARNING);

            else {
                if (error == null) {
                    String message = getString(R.string.device_added_congratulations_text);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", message);
                    Intent intent = new Intent(SignInActivity.this, PermissionInformationActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } else
                    showDialog(ERROR);
            }
        }

    }
}
