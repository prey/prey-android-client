/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;

import androidx.appcompat.app.AppCompatActivity;

import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.js.CustomWebView;
import com.prey.activities.js.WebAppInterface;

public class SecurityActivity extends AppCompatActivity {

    private CustomWebView myWebView = null;

    public void onBackPressed() {
        Intent intent = null;
        intent = new Intent(getApplication(), CheckPasswordHtmlActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        }catch (Exception e){}
        setContentView(R.layout.webview);
        PreyLogger.d("SecurityActivity: onCreate");
        security();
    }

    public void settings() {
        PreyLogger.d("CheckPasswordHtmlActivity: settings");
        myWebView = (CustomWebView) findViewById(R.id.install_browser);
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
    }

    public void security() {
        PreyLogger.d("CheckPasswordHtmlActivity: tryReport");
        String lng = PreyUtils.getLanguage();
        String url = CheckPasswordHtmlActivity.URL_ONB + "#/" + lng + "/security";
        settings();
        PreyLogger.d("_url:" + url);
        myWebView.addJavascriptInterface(new WebAppInterface(this), CheckPasswordHtmlActivity.JS_ALIAS);
        myWebView.loadUrl(url);
    }

}
