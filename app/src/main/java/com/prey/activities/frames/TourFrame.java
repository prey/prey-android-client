/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.frames;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.WelcomeActivity;
import com.prey.activities.javascript.PreyJavaScriptInterface;

public class TourFrame extends Fragment {

    private WelcomeActivity welcome;

    @Override
    public void onResume() {
        PreyLogger.i("onResume of TourFrame");
        super.onResume();
    }

    @Override
    public void onPause() {
        PreyLogger.i("OnPause of TourFrame");
        super.onPause();
    }

    public void setActivity(WelcomeActivity welcome) {
        this.welcome = welcome;
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_tour, container, false);
        WebView mWebView = (WebView) view.findViewById(R.id.install_browser);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        PreyJavaScriptInterface webAppInterface = new PreyJavaScriptInterface(getActivity());
        webAppInterface.setActivity(welcome);
        mWebView.addJavascriptInterface(webAppInterface, "Android");
        mWebView.loadUrl("file:///android_asset/www/index.html");
        return view;
    }

}
