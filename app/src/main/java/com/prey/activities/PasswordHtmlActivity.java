/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.js.WebAppInterface;

public class PasswordHtmlActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        PreyLogger.d("PasswordHtmlActivity: onCreate");

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

        myWebView.addJavascriptInterface(new WebAppInterface(this,this), CheckPasswordHtmlActivity.JS_ALIAS);
        myWebView.loadUrl(url);
        myWebView.loadUrl("javascript:window.location.reload(true)");

    }



    @Override
    protected void onResume() {
        super.onResume();
        String unlockPass=PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
        String pinNumber=PreyConfig.getPreyConfig(getApplicationContext()).getPinNumber();
        String pinActivated=PreyConfig.getPreyConfig(getApplicationContext()).getPinActivated();
        boolean isLock = unlockPass!=null&&!"".equals(unlockPass);
        boolean isPinActivated = pinNumber!=null&&!"".equals(pinNumber)&&pinActivated!=null&&!"".equals(pinActivated);
        PreyLogger.d("PasswordHtmlActivity isLock:" + isLock+" isPinActivated:"+isPinActivated);
        if(!isLock&&!isPinActivated){
            finishAffinity();
        }
    }

    public void pfinish(){
        finishAffinity();
    }


}