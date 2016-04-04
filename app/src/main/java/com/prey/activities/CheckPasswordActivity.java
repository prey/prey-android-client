/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import java.util.Locale;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.util.KeyboardStatusDetector;
import com.prey.util.KeyboardVisibilityListener;

public class CheckPasswordActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    int wrongPasswordIntents = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        setContentView(R.layout.password2);
        bindPasswordControls();
        TextView device_ready_h2_text=(TextView)findViewById(R.id.device_ready_h2_text);
        final  TextView textForgotPassword = (TextView) findViewById(R.id.link_forgot_password);

        Button password_btn_login=(Button)findViewById(R.id.password_btn_login);
        EditText password_pass_txt=(EditText)findViewById(R.id.password_pass_txt);

        TextView textView1=(TextView)findViewById(R.id.textView1);
        TextView textView2=(TextView)findViewById(R.id.textView2);


        Typeface titilliumWebRegular = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
        Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");


        textView1.setTypeface(magdacleanmonoRegular);
        textView2.setTypeface(magdacleanmonoRegular);


        device_ready_h2_text.setTypeface(titilliumWebRegular);
        textForgotPassword.setTypeface(titilliumWebBold);
        password_btn_login.setTypeface(titilliumWebBold);
        password_pass_txt.setTypeface(magdacleanmonoRegular);

        try {

            textForgotPassword.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    try {
                        String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
                        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {
        }



        TextView textView5_1=(TextView)findViewById(R.id.textView5_1);
        TextView textView5_2=(TextView)findViewById(R.id.textView5_2);

        textView5_1.setTypeface(magdacleanmonoRegular);
        textView5_2.setTypeface(titilliumWebBold);


        TextView textViewUninstall=(TextView) findViewById(R.id.textViewUninstall);
        LinearLayout linearLayoutTour = (LinearLayout) findViewById(R.id.linearLayoutTour);
        textViewUninstall.setTypeface(titilliumWebBold);


        if(PreyConfig.getPreyConfig(getApplication()).getProtectTour()) {
            linearLayoutTour.setVisibility(View.GONE);
            textViewUninstall.setVisibility(View.VISIBLE);

            textViewUninstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = PreyConfig.getPreyConfig(getApplication()).getPreyUninstallUrl();

                    Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    startActivity(browserIntent);

                    finish();
                }
            });
        }else{

            linearLayoutTour.setVisibility(View.VISIBLE);
            textViewUninstall.setVisibility(View.GONE);
            try {

                linearLayoutTour.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplication(), TourActivity1.class);
                        Bundle b = new Bundle();
                        b.putInt("id", 1);
                        intent.putExtras(b);
                        startActivity(intent);
                        finish();
                    }
                });
            }catch (Exception e){

            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            PreyConfig.getPreyConfig(this).setCanAccessCamara(PreyPermission.canAccessCamera(this));
            PreyConfig.getPreyConfig(this).setCanAccessCoarseLocation(PreyPermission.canAccessCoarseLocation(this));
            PreyConfig.getPreyConfig(this).setCanAccessFineLocation(PreyPermission.canAccessFineLocation(this));
            PreyConfig.getPreyConfig(this).setCanAccessReadPhoneState(PreyPermission.canAccessReadPhoneState(this));

            if(!PreyPermission.canAccessFineLocation(this)||!PreyPermission.canAccessCoarseLocation(this)||!PreyPermission.canAccessCamera(this)
                    || !PreyPermission.canAccessReadPhoneState(this)|| !PreyPermission.canAccessReadExternalStorage(this)){


                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final FrameLayout frameView = new FrameLayout(this);
                builder.setView(frameView);

                final AlertDialog alertDialog = builder.create();
                LayoutInflater inflater = alertDialog.getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.warning, frameView);

                TextView warning_title=(TextView)dialoglayout.findViewById(R.id.warning_title);
                TextView warning_body=(TextView)dialoglayout.findViewById(R.id.warning_body);

                warning_title.setTypeface(magdacleanmonoRegular);
                warning_body.setTypeface(titilliumWebBold);


                Button button_ok = (Button) dialoglayout.findViewById(R.id.button_ok);
                Button button_close = (Button) dialoglayout.findViewById(R.id.button_close);
                button_ok.setTypeface(titilliumWebBold);
                button_close.setTypeface(titilliumWebBold);

                final Activity thisActivity=this;
                button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        askForPermission();
                        alertDialog.dismiss();

                    }
                });

                button_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        alertDialog.dismiss();
                    }
                });


                alertDialog.show();
            }


        }
    }


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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            new CheckPassword().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,passwordtyped);
                        else
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


    protected class CheckPassword extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;
        boolean isPasswordOk = false;
        String error = null;


        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(CheckPasswordActivity.this);
                progressDialog.setMessage(CheckPasswordActivity.this.getText(R.string.password_checking_dialog).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            } catch (Exception e) {

            }
        }

        @Override
        protected Void doInBackground(String... password) {
            try {
                String apikey = PreyConfig.getPreyConfig(CheckPasswordActivity.this).getApiKey();
                PreyLogger.d("apikey:"+apikey+" password[0]:"+password[0]);
                isPasswordOk = PreyWebServices.getInstance().checkPassword(CheckPasswordActivity.this, apikey, password[0]);
                if(isPasswordOk) {
                    PreyConfig.getPreyConfig(CheckPasswordActivity.this).setTimePasswordOk();
                    PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_LOGIN_SETTINGS);
                } else {
                    PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_FAILED_LOGIN_SETTINGS);
                }
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
                Toast.makeText(CheckPasswordActivity.this, error, Toast.LENGTH_LONG).show();
            else if (!isPasswordOk) {

                wrongPasswordIntents++;
                if (wrongPasswordIntents == 3) {
                    Toast.makeText(CheckPasswordActivity.this, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                } else {
                    Toast.makeText(CheckPasswordActivity.this, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                }

            } else {
                Intent intent = new Intent(CheckPasswordActivity.this, DeviceReadyActivity.class);
                PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                startActivity(intent);
                new Thread(new EventManagerRunner(CheckPasswordActivity.this, new Event(Event.APPLICATION_OPENED))).start();
            }
        }

    }


    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermission() {
        ActivityCompat.requestPermissions(CheckPasswordActivity.this, INITIAL_PERMS, REQUEST_PERMISSIONS);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PreyLogger.i("_______onRequestPermissionsResult_______requestCode:" + requestCode + " permissions:" + permissions.toString() + " grantResults:" + grantResults.toString());

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessCamara(true);
                }
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessFineLocation(true);
                }
                if (grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessCoarseLocation(true);
                }
                if (grantResults[3] ==  PackageManager.PERMISSION_GRANTED){
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessReadPhoneState(true);
                }
                if (grantResults[4] ==  PackageManager.PERMISSION_GRANTED){
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessExternalStorage(true);
                }
                return;
            }
        }

    }

    private static final int REQUEST_PERMISSIONS = 5;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


}
