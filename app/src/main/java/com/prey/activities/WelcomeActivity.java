/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.frames.DeviceReadyFrame;
import com.prey.activities.frames.PrivilegesErrorFrame;
import com.prey.activities.frames.MenuFrame;
import com.prey.activities.frames.SignInFrame;
import com.prey.activities.frames.SignUpFrame;
import com.prey.activities.frames.TourFrame;
import com.prey.backwardcompatibility.FroyoSupport;

public class WelcomeActivity extends FragmentActivity {

    @Override
    public void onResume() {
        PreyLogger.i("onResume of WelcomeActivity");
        super.onResume();
    }

    @Override
    public void onPause() {
        PreyLogger.i("onPause of WelcomeActivity");
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        try {
            Button newUser = (Button) findViewById(R.id.btn_welcome_newuser);

            newUser.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(WelcomeActivity.this, CreateAccountActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } catch (Exception e) {
        }

        try {
            Button oldUser = (Button) findViewById(R.id.btn_welcome_olduser);
            oldUser.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(WelcomeActivity.this, AddDeviceToAccountActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } catch (Exception e) {
        }
    }

    protected void onCreate2(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_frame);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        menu();
    }

    public void tour() {

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            TourFrame tourFrame = new TourFrame();
            tourFrame.setActivity(this);

            FragmentTransaction localFragmentTransaction2 = getSupportFragmentManager().beginTransaction();
            localFragmentTransaction2.replace(R.id.tourFrame, tourFrame);
            localFragmentTransaction2.commitAllowingStateLoss();

            FrameLayout tourFrame1 = (FrameLayout) findViewById(R.id.tourFrame);
            tourFrame1.setVisibility(View.VISIBLE);
            FrameLayout protectFrame1 = (FrameLayout) findViewById(R.id.protectFrame);
            protectFrame1.setVisibility(View.GONE);
        } else {
            Intent intent = new Intent(getApplicationContext(), TourActivity.class);
            Bundle b = new Bundle();
            b.putInt("id", 1);
            intent.putExtras(b);
            startActivity(intent);
            finish();
        }

    }

    public void signIn() {

        SignInFrame signInFrame = new SignInFrame();
        signInFrame.setActivity(this);

        FragmentTransaction localFragmentTransaction1 = getSupportFragmentManager().beginTransaction();

        localFragmentTransaction1.replace(R.id.protectFrame, signInFrame);
        localFragmentTransaction1.commitAllowingStateLoss();

        FrameLayout protectFrame1 = (FrameLayout) findViewById(R.id.protectFrame);
        protectFrame1.setVisibility(View.VISIBLE);
        FrameLayout tourFrame1 = (FrameLayout) findViewById(R.id.tourFrame);
        tourFrame1.setVisibility(View.GONE);
    }

    public void signUp() {
        SignUpFrame signUpFrame = new SignUpFrame();
        signUpFrame.setActivity(this);

        FragmentTransaction localFragmentTransaction1 = getSupportFragmentManager().beginTransaction();

        localFragmentTransaction1.replace(R.id.protectFrame, signUpFrame);
        localFragmentTransaction1.commitAllowingStateLoss();

        FrameLayout protectFrame1 = (FrameLayout) findViewById(R.id.protectFrame);
        protectFrame1.setVisibility(View.VISIBLE);
        FrameLayout tourFrame1 = (FrameLayout) findViewById(R.id.tourFrame);
        tourFrame1.setVisibility(View.GONE);
    }

    public void menu() {
        if (PreyConfig.getPreyConfig(this).getProtectReady()) {
            ready();
        } else {

            new RegisterInitTask().execute();
            MenuFrame menuFrame = new MenuFrame();
            menuFrame.setActivity(this);

            FragmentTransaction localFragmentTransaction1 = getSupportFragmentManager().beginTransaction();
            localFragmentTransaction1.replace(R.id.protectFrame, menuFrame);
            localFragmentTransaction1.commitAllowingStateLoss();


            FrameLayout protectFrame1 = (FrameLayout) findViewById(R.id.protectFrame);
            protectFrame1.setVisibility(View.VISIBLE);
            FrameLayout tourFrame1 = (FrameLayout) findViewById(R.id.tourFrame);
            tourFrame1.setVisibility(View.GONE);
        }
    }

    public void privileges() {
        PrivilegesErrorFrame privilegesErrorFrame = new PrivilegesErrorFrame();
        privilegesErrorFrame.setActivity(this);

        FragmentTransaction localFragmentTransaction1 = getSupportFragmentManager().beginTransaction();
        localFragmentTransaction1.replace(R.id.protectFrame, privilegesErrorFrame);
        localFragmentTransaction1.commitAllowingStateLoss();

        FrameLayout protectFrame1 = (FrameLayout) findViewById(R.id.protectFrame);
        protectFrame1.setVisibility(View.VISIBLE);
        FrameLayout tourFrame1 = (FrameLayout) findViewById(R.id.tourFrame);
        tourFrame1.setVisibility(View.GONE);
    }

    private static final int SECURITY_PRIVILEGES = 10;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PreyLogger.d("requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == SECURITY_PRIVILEGES) {
            menu();
            PreyConfig.getPreyConfig(getApplicationContext()).setProtectPrivileges(true);
        }
    }

    public void addPrivileges() {
        Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
        startActivityForResult(intent, SECURITY_PRIVILEGES);
    }

    public void ready() {
        DeviceReadyFrame deviceReadyFrame = new DeviceReadyFrame();
        deviceReadyFrame.setActivity(this);

        FragmentTransaction localFragmentTransaction2 = getSupportFragmentManager().beginTransaction();
        localFragmentTransaction2.replace(R.id.protectFrame, deviceReadyFrame);
        localFragmentTransaction2.commitAllowingStateLoss();

        FrameLayout protectFrame1 = (FrameLayout) findViewById(R.id.protectFrame);
        protectFrame1.setVisibility(View.VISIBLE);
        FrameLayout tourFrame1 = (FrameLayout) findViewById(R.id.tourFrame);
        tourFrame1.setVisibility(View.GONE);


    }


    private class RegisterInitTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... data) {
           /*
            try {
               PreyWebServices.getInstance().registerInit(getApplicationContext());


            } catch (Exception e) {
                PreyLogger.e("Error, causa:"+e.getMessage(),e);
            }
            */
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {

        }

    }
}
