/*******************************************************************************
m * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;

import android.content.Context;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.prey.PreyConfig;
import com.prey.PreyUtils;
import com.prey.R;

import com.prey.activities.browser.javascript.PreyJavaScriptInterface;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;

public class LoginActivity extends Activity {

	WebView installBrowser =null;
			
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.install_browser);
		
		// Delete notifications (in case Activity was started by one of them)
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(R.string.preyForAndroid_name);
		startup();
		if (isThisDeviceAlreadyRegisteredWithPrey()) {
			Event event = new Event(Event.APPLICATION_OPENED);
			new Thread(new EventManagerRunner(getApplicationContext(), event));
		}

	 
		 
	}

	@Override
	protected void onStart() {
		super.onStart();
		startup();
	}

	private void startup() {
		Context ctx = getApplicationContext();
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		if (!isThisDeviceAlreadyRegisteredWithPrey()) {
			if (preyConfig.isActiveTour()) {
				tourBrowser();
			} else {
				if (PreyConfig.getPreyConfig(ctx).isActiveWizard()) {
					warningBrowser();
				} else {
					readyBrowser();
				}
			}
		} else {

			if (preyConfig.isFroyoOrAbove() && !FroyoSupport.getInstance(ctx).isAdminActive()) {
				warningBrowser();
			} else {
				readyBrowser();
			}
		}

	}

	private boolean isThisDeviceAlreadyRegisteredWithPrey() {
		return PreyConfig.getPreyConfig(this).isThisDeviceAlreadyRegisteredWithPrey(false);
	}

	public void readyBrowser() {
		setWebView("file:///android_asset/v2/ok.html");

	}

	public void tourBrowser() {
		setWebView("file:///android_asset/v2/index.html");
	}

	public void warningBrowser() {
		setWebView("file:///android_asset/v2/error.html");
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void setWebView(String url) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		PreyJavaScriptInterface myJavaScriptInterface = null;
		myJavaScriptInterface = new PreyJavaScriptInterface(this, PreyUtils.getDeviceType(this));
		installBrowser = (WebView) findViewById(R.id.install_browser);
		installBrowser.setBackgroundColor(0x00000000);
		installBrowser.clearCache(true);
		installBrowser.clearHistory();
		installBrowser.loadUrl(url);
		installBrowser.getSettings().setLoadWithOverviewMode(true);
		installBrowser.getSettings().setUseWideViewPort(true);
		installBrowser.setWebChromeClient(new WebChromeClient());
		installBrowser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		installBrowser.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");
		installBrowser.getSettings().setJavaScriptEnabled(true);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
