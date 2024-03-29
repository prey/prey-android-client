/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.js.CustomWebView;
import com.prey.activities.js.WebAppInterface;

public class PasswordHtmlActivity extends Activity {

    private WebView myWebView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        PreyLogger.d("PasswordHtmlActivity: onCreate");
        myWebView = (WebView) findViewById(R.id.install_browser);
        myWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                CustomWebView.callDispatchKeyEvent(getApplicationContext(),keyEvent);
                return false;
            }
        });
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        String lng = PreyUtils.getLanguage();
        String url = "%s#/%s/%s";
        String lockMessage = PreyConfig.getPreyConfig(this).getLockMessage();
        if (lockMessage != null && !"".equals(lockMessage)) {
            url = String.format(url, CheckPasswordHtmlActivity.URL_ONB, lng, "lockmessage");
        } else {
            url = String.format(url, CheckPasswordHtmlActivity.URL_ONB, lng, "lock");
        }
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), CheckPasswordHtmlActivity.JS_ALIAS);
        myWebView.loadUrl(url);
        myWebView.loadUrl("javascript:window.location.reload(true)");
        PreyConfig.getPreyConfig(this).viewLock = myWebView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String unlockPass = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
        boolean isLock = unlockPass != null && !"".equals(unlockPass);
        PreyLogger.d("PasswordHtmlActivity isLock:" + isLock);
        if (!isLock) {
            finishAffinity();
        }
    }

    public void pfinish() {
        finishAffinity();
    }
}