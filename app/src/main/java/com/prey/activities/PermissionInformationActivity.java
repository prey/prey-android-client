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
    private boolean first = false;

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
                Intent intent = new Intent(PermissionInformationActivity.this, WelcomeActivity.class);
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
                setContentView(R.layout.permission_information_error2);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Button buttonActivate = (Button) findViewById(R.id.buttonActivate);
                buttonActivate.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        first = true;
                        Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
                        startActivityForResult(intent, SECURITY_PRIVILEGES);
                        PreyWebServices.getInstance().sendEvent(getApplicationContext(), PreyConfig.ANDROID_PRIVILEGES_GIVEN);
                    }
                });


                Typeface titilliumWebRegular = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
                Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
                Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");


                TextView textView1 = (TextView) findViewById(R.id.textView1);
                TextView textView2 = (TextView) findViewById(R.id.textView2);
                TextView textView3 = (TextView) findViewById(R.id.textView3);


                textView1.setTypeface(magdacleanmonoRegular);
                textView2.setTypeface(magdacleanmonoRegular);
                textView3.setTypeface(titilliumWebRegular);


                buttonActivate.setTypeface(titilliumWebBold);


            }

    }
}

