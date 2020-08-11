/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.js.WebAppInterface;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class PasswordHtmlActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.webview);
        PreyLogger.d("PasswordActivity3: onCreate");



        WebView myWebView = (WebView) findViewById(R.id.install_browser);
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);

        String lng = PreyUtils.getLanguage();
        String url = CheckPasswordHtmlActivity.URL_ONB + "#/" + lng + "/lock";

        myWebView.addJavascriptInterface(new WebAppInterface(this), CheckPasswordHtmlActivity.JS_ALIAS);
        myWebView.loadUrl(url);
        myWebView.loadUrl("javascript:window.location.reload(true)");

    }

    @Override
    protected void onResume() {
        super.onResume();
        String unlock = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
        PreyLogger.d("PasswordActivity3 unlock:" + unlock);
        if (unlock == null || "".equals(unlock)) {
            Intent intent = new Intent(getApplicationContext(), CloseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }



}