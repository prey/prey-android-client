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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.prey.PreyApp;
import com.prey.PreyStatus;
import com.prey.actions.aware.AwareConfig;
import com.prey.actions.aware.AwareController;
import com.prey.barcodereader.BarcodeActivity;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.preferences.RunBackgroundCheckBoxPreference;
import com.prey.util.KeyboardStatusDetector;
import com.prey.util.KeyboardVisibilityListener;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends Activity {


    private static final int NO_MORE_DEVICES_WARNING = 0;
    private static final int ERROR = 3;
    private String error = null;
    private boolean noMoreDeviceError = false;


    @Override
    public void onResume() {
        PreyLogger.d("onResume of SignInActivity");
        super.onResume();

    }

    @Override
    public void onPause() {
        PreyLogger.d("onPause of SignInActivity");
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
        PreyLogger.d("onCreate of SignInActivity");

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
                    PreyLogger.e("error:"+e.getMessage(),e);
                }
            }
        });




        buttonSignin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    new AddDeviceToAccount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, email, password, PreyUtils.getDeviceType(ctx));
                else
                    new AddDeviceToAccount().execute(email, password, PreyUtils.getDeviceType(ctx));
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

    private class AddDeviceToAccount extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(SignInActivity.this);
                progressDialog.setMessage(SignInActivity.this.getText(R.string.set_old_user_loading).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }catch (Exception e){}
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                noMoreDeviceError = false;
                error = null;
                PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceToAccount(SignInActivity.this, data[0], data[1], data[2]);
                final Context ctx=getApplicationContext();
                PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
                PreyConfig.getPreyConfig(ctx).registerC2dm();
                PreyWebServices.getInstance().sendEvent(ctx, PreyConfig.ANDROID_SIGN_IN);
                String email=PreyWebServices.getInstance().getEmail(ctx);
                PreyConfig.getPreyConfig(ctx).setEmail(email);
                PreyConfig.getPreyConfig(ctx).setRunBackground(true);
                RunBackgroundCheckBoxPreference.notifyReady(ctx);
                new PreyApp().run(ctx);
                try {
                    Map<String, Object> eventValue = new HashMap<String, Object>();
                    eventValue.put(AFInAppEventParameterName.SUCCESS,true);
                    AppsFlyerLib.getInstance().trackEvent(ctx, AFInAppEventType.LOGIN,eventValue);
                } catch (Exception e1) {}
            } catch (Exception e) {
                PreyLogger.e("error:"+e.getMessage(),e);
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                if(progressDialog!=null)
                    progressDialog.dismiss();
                if (error == null) {
                        String message = getString(R.string.device_added_congratulations_text);
                        Bundle bundle = new Bundle();
                        bundle.putString("message", message);
                        Intent intent =null;
                        if (PreyConfig.getPreyConfig(SignInActivity.this).isChromebook()) {
                            intent = new Intent(SignInActivity.this, WelcomeActivity.class);
                            PreyConfig.getPreyConfig(SignInActivity.this).setProtectReady(true);
                        }else {
                            intent = new Intent(SignInActivity.this, PermissionInformationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                } else {
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder( SignInActivity.this);
                    alertDialog.setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setCancelable(false);
                    alertDialog.show();
                }
            } catch (Exception e) {
            }
        }

    }

}
