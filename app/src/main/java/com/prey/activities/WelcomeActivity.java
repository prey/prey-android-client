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
import android.os.Bundle;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.backwardcompatibility.FroyoSupport;

public class WelcomeActivity extends FragmentActivity {

    @Override
    public void onResume() {
        PreyLogger.d("onResume of WelcomeActivity");
        super.onResume();
        menu();
    }

    @Override
    public void onPause() {
        PreyLogger.d("onPause of WelcomeActivity");
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
        PreyLogger.d("onCreate of WelcomeActivity");
        menu();

    }


    public void menu() {
        PreyLogger.d("menu WelcomeActivity");
        String email = PreyConfig.getPreyConfig(this).getEmail();
        if(email==null||"".equals(email)){
            PreyLogger.d("email:"+email);
            PreyConfig.getPreyConfig(this).setProtectReady(false);
            PreyConfig.getPreyConfig(this).setProtectAccount(false);
            PreyConfig.getPreyConfig(this).setProtectTour(false);
        }
        if (PreyConfig.getPreyConfig(this).isThisDeviceAlreadyRegisteredWithPrey()){
            ready();
        } else {
            Intent intent = new Intent(getApplicationContext(), InitActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void ready() {
        PreyLogger.d("ready WelcomeActivity");
        Intent intent = new Intent(getApplicationContext(), CheckPasswordHtmlActivity.class);
        startActivity(intent);
        finish();
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





}
