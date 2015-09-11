/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.prey.R;
import com.prey.activities.javascript.WebAppInterface;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);

        WebView myWebView = (WebView) this.findViewById(R.id.install_browser);

        // Bind a new interface between your JavaScript and Android code
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // Enable JavaScript
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load HTML page
        myWebView.loadUrl("file:///android_asset/www/example.html");

    }

}
