/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2021 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;

public class ChromeActivity extends AppCompatActivity {

    public static String JS_ALIAS="Android";
    public static String URL_ONB = "file:///android_asset/html/index.html";

    private WebView myWebView = null;

    public static int OVERLAY_PERMISSION_REQ_CODE = 5469;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreyLogger.d("ChromeActivity: onCreate");
        try {
            getSupportActionBar().hide();
        }catch (Exception e){
            PreyLogger.e("Error ActionBar().hide",e);
        }
        setContentView(R.layout.webview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreyLogger.d("ChromeActivity: onResume");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        String lng = PreyUtils.getLanguage();
        String url = URL_ONB + "#/" + lng + "/chrome";
        myWebView = (WebView) findViewById(R.id.install_browser);
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        myWebView.loadUrl(url);
        myWebView.loadUrl("javascript:window.location.reload(true)");
    }

    protected void onDestroy() {
        super.onDestroy();
    }

}