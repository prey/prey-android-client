/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.prey.PreyConfig;
import com.prey.R;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.prey.PreyLogger;
import com.prey.actions.aware.AwareController;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.net.PreyWebServices;

public class PermissionInformationActivity extends PreyActivity {

    private static final int SECURITY_PRIVILEGES = 10;
    private String congratsMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle bundle = getIntent().getExtras();
        congratsMessage = bundle.getString("message");
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        showScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PreyLogger.d("requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == SECURITY_PRIVILEGES)
            showScreen();
    }

    private void showScreen() {



            if (FroyoSupport.getInstance(this).isAdminActive()) {
                Intent intent = new Intent(PermissionInformationActivity.this, CheckPasswordHtmlActivity.class);
                PreyConfig.getPreyConfig(PermissionInformationActivity.this).setProtectReady(true);
                new Thread() {
                    public void run() {
                        try{
                            AwareController.getInstance().init(getApplicationContext());
                        }catch(Exception e){
                        }
                    }
                }.start();
                startActivity(intent);
                finish();
            } else {


                        Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
                        startActivityForResult(intent, SECURITY_PRIVILEGES);
                        PreyWebServices.getInstance().sendEvent(getApplicationContext(), PreyConfig.ANDROID_PRIVILEGES_GIVEN);



            }

    }
}

