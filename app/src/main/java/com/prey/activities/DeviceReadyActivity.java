/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.R;

public class DeviceReadyActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {



    @Override
    protected void onResume()
    {
        super.onResume();

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancel(PreyConfig.TAG,PreyConfig.NOTIFY_ANDROID_6);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.device_ready);

        PreyLogger.i("onCreate of DeviceReadyActivity");

        TextView textView6=(TextView) findViewById(R.id.textView6);
        final Typeface titilliumWebRegular = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
        final Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
        final Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");

        TextView textView1=(TextView)findViewById(R.id.textView1);
        TextView textView2=(TextView)findViewById(R.id.textView2);
        TextView textView3_1=(TextView)findViewById(R.id.textView3_1);
        TextView textView3_2=(TextView)findViewById(R.id.textView3_2);
        TextView textView4_1=(TextView)findViewById(R.id.textView4_1);
        TextView textView4_2=(TextView)findViewById(R.id.textView4_2);
        TextView textView5_1=(TextView)findViewById(R.id.textView5_1);
        TextView textView5_2=(TextView)findViewById(R.id.textView5_2);

        textView1.setTypeface(magdacleanmonoRegular);
        textView2.setTypeface(magdacleanmonoRegular);
        textView3_1.setTypeface(magdacleanmonoRegular);
        textView3_2.setTypeface(titilliumWebBold);
        textView4_1.setTypeface(magdacleanmonoRegular);
        textView4_2.setTypeface(titilliumWebBold);
        textView5_1.setTypeface(magdacleanmonoRegular);
        textView5_2.setTypeface(titilliumWebBold);
        textView6.setTypeface(titilliumWebBold);

        LinearLayout linearLayout1=(LinearLayout)findViewById(R.id.linearLayout1);
        linearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = PreyConfig.getPreyConfig(getApplication()).getPreyPanelUrl();
                    Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    startActivity(browserIntent);
                    finish();
                } catch (Exception e) {
                    PreyLogger.i("error:"+e.getMessage());
                }
            }
        });

        LinearLayout linearLayout2=(LinearLayout)findViewById(R.id.linearLayout2);
        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
                    intent = new Intent(getApplication(), CheckPasswordActivity.class);
                } else {
                    intent = new Intent(getApplication(), PreyConfigurationActivity.class);
                }
                startActivity(intent);
                finish();
            }
        });

        LinearLayout linearLayout3 = (LinearLayout) findViewById(R.id.linearLayout3);






        if(PreyConfig.getPreyConfig(getApplication()).getProtectTour()) {
            linearLayout3.setVisibility(View.GONE);
            textView6.setVisibility(View.VISIBLE);

            textView6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = PreyConfig.getPreyConfig(getApplication()).getPreyUninstallUrl();

                    Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    startActivity(browserIntent);

                    finish();
                }
            });
        }else{

            linearLayout3.setVisibility(View.VISIBLE);
            textView6.setVisibility(View.GONE);
            try {

                linearLayout3.setOnClickListener(new View.OnClickListener() {
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

            if(!PreyPermission.canAccessFineLocation(this)||!PreyPermission.canAccessCoarseLocation(this)||!PreyPermission.canAccessCamera(this)|| !PreyPermission.canAccessReadPhoneState(this)){


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

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermission() {
        ActivityCompat.requestPermissions(this, INITIAL_PERMS, REQUEST_PERMISSIONS);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         PreyLogger.i("_______onRequestPermissionsResult_______requestCode:" + requestCode + " permissions:" + permissions.toString() + " grantResults:" + grantResults.toString());

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                     PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessCamara(true);
                    PreyLogger.i("______setCanAccessCamara");
                }
                if (grantResults[1] ==
                        PackageManager.PERMISSION_GRANTED){
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessFineLocation(true);
                }
                if (grantResults[2] ==
                        PackageManager.PERMISSION_GRANTED){
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessCoarseLocation(true);
                }
                if (grantResults[3] ==
                        PackageManager.PERMISSION_GRANTED){
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessReadPhoneState(true);
                }
                return;
            }
        }


    }




    private static final int REQUEST_PERMISSIONS = 1;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };




}
