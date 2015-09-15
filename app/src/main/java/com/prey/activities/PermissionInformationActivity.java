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

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.prey.PreyLogger;
import com.prey.activities.javascript.WebAppInterface;
import com.prey.backwardcompatibility.FroyoSupport;

import java.util.Locale;

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
        PreyLogger.d("first:" + first);
        if (getPreyConfig().isFroyoOrAbove() && !FroyoSupport.getInstance(this).isAdminActive() && !first) {
            first = true;
            Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
            startActivityForResult(intent, SECURITY_PRIVILEGES);
        } else {
            first = false;
            showScreen();
        }
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

                    startActivity(intent);
                    PreyConfig.getPreyConfig(PermissionInformationActivity.this).setProtectReady(true);

        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                WebView myWebView = (WebView) findViewById(R.id.install_browser);
                myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
                WebSettings webSettings = myWebView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                String idioma="";
                if("es".equals(Locale.getDefault().getLanguage()))
                    idioma="_es";
                myWebView.loadUrl("file:///android_asset/www/permission"+idioma+".html");
            }else {
                setContentView(R.layout.permission_information_error2);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Button give = (Button) findViewById(R.id.buttonActivate);
                give.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        first = true;
                        Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
                        startActivityForResult(intent, SECURITY_PRIVILEGES);
                    }
                });
            }

           PreyConfig.getPreyConfig(this).registerC2dm();
        }
    }
}

