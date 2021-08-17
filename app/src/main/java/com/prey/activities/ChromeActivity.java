/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.js.CustomWebView;
import com.prey.activities.js.WebAppInterface;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.services.PreyAccessibilityService;
import com.prey.services.PreyOverlayService;

public class ChromeActivity extends AppCompatActivity {

    public static String JS_ALIAS="Android";
    public static String URL_ONB = "file:///android_asset/html/index.html";

    private WebView myWebView = null;

    public static int OVERLAY_PERMISSION_REQ_CODE = 5469;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        }catch (Exception e){
            PreyLogger.e("Error ActionBar().hide",e);
        }
        setContentView(R.layout.webview);
        PreyLogger.d("ChromeActivity: onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreyLogger.d("CheckPasswordHtmlActivity: onResume");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        String lng = PreyUtils.getLanguage();
        String url = URL_ONB + "#/" + lng + "/chrome";
        settings();
        PreyLogger.d("_url:" + url);
        myWebView.loadUrl(url);
        myWebView.loadUrl("javascript:window.location.reload(true)");
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void settings() {
        PreyLogger.d("CheckPasswordHtmlActivity: settings");
        myWebView = (WebView) findViewById(R.id.install_browser);
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try{
                myWebView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                myWebView.getSettings().setSavePassword(false);
                myWebView.clearFormData();
            }catch (Exception e){
                PreyLogger.e("Error autofill:"+e.getMessage(),e);
            }
        }
    }

}