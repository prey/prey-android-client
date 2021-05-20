/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.js.CustomWebView;

public class PanelWebActivity extends Activity {

    private final Activity activity = this;
    private WebView myWebView = null;

    public void onBackPressed() {
        Intent intent = null;
        intent = new Intent(getApplication(), CheckPasswordHtmlActivity.class);
        startActivity(intent);
        finish();

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panelweb);
        this.setContentView(R.layout.activity_webview);
        myWebView = (WebView) findViewById(R.id.install_browser);
        myWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                CustomWebView.callDispatchKeyEvent(getApplicationContext(),keyEvent);
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        WebSettings settings = myWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        myWebView.setVerticalScrollBarEnabled(false);
        myWebView.setHorizontalScrollBarEnabled(false);
        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setTitle("Loading...");
                activity.setProgress(progress * 100);
                if (progress == 100)
                    activity.setTitle(R.string.app_name);
            }


        });
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PreyLogger.d("Finished:"+url);
                super.onPageFinished(view, url);
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                PreyLogger.d("Started:"+url);
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                PreyLogger.d("OverrideUrl:"+url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelJwt();
        String tokenJwt=PreyConfig.getPreyConfig(getApplicationContext()).getTokenJwt();
        PreyLogger.d("tokenJwt:"+tokenJwt);
        String postData = "token="+tokenJwt;
        myWebView.postUrl(url, postData.getBytes());
    }

}