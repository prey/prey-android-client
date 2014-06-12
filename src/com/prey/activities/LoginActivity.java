/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.annotation.SuppressLint;
import android.app.NotificationManager;

import com.prey.PreyUtils;
import com.prey.PreyVerify;
import com.prey.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;

import com.prey.PreyConfig;
import com.prey.activities.browser.javascript.PreyJavaScriptInterface;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.services.PreyDisablePowerOptionsService;

public class LoginActivity extends PasswordActivity {

	private WebView installBrowser =null;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Delete notifications (in case Activity was started by one of them)
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(R.string.preyForAndroid_name);
		startup();
		
		boolean disablePowerOptions = PreyConfig.getPreyConfig(getApplicationContext()).isDisablePowerOptions();
		if (disablePowerOptions) {
			startService(new Intent(getApplicationContext(), PreyDisablePowerOptionsService.class));
		}else{
			stopService(new Intent(getApplicationContext(), PreyDisablePowerOptionsService.class));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		startup();
	}

	private void startup() {
		Context ctx=getApplicationContext();
		if (!isThisDeviceAlreadyRegisteredWithPrey()) {
			Intent intent =null;
			if (!isThereBatchInstallationKey()){
				if (PreyConfig.getPreyConfig(ctx).isActiveTour()) {
					tourBrowser();
				} else {
					if (PreyConfig.getPreyConfig(ctx).isActiveWizard()) {
						warningBrowser();
					} else {
						readyBrowser();
					}
				}
			}else{
				intent = new Intent(ctx, WelcomeBatchActivity.class);
				startActivity(intent);
				finish();
			}
		} else {
			PreyVerify.getInstance(this);
			if(getPreyConfig().showFeedback()){
				showFeedback(getApplicationContext());
			}else{
				if (PreyConfig.getPreyConfig(ctx).isFroyoOrAbove() && !FroyoSupport.getInstance(ctx).isAdminActive()) {
					warningBrowser();
				} else {
					readyBrowser();
				}
			}
		}
	}

	private void showLogin() {
		setContentView(R.layout.login);
		updateLoginScreen();
		Button gotoCP = (Button) findViewById(R.id.login_btn_cp);
		Button gotoSettings = (Button) findViewById(R.id.login_btn_settings);

		gotoCP.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				try{
					String url=PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
					Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
					startActivity(browserIntent);
				}catch(Exception e){
				}
			}
		});

		gotoSettings.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, CheckPasswordActivity.class);
				startActivity(intent);
			}
		});
	}
	 
	private boolean isThisDeviceAlreadyRegisteredWithPrey() {
		return getPreyConfig().isThisDeviceAlreadyRegisteredWithPrey(false);
	}

	
	private void showFeedback(Context ctx){
		Intent popup = new Intent(ctx, FeedbackActivity.class);
		popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(popup);
	}
	
	private boolean isThereBatchInstallationKey() {
		String apiKeyBatch=getPreyConfig().getApiKeyBatch();
		return (apiKeyBatch!=null&&!"".equals(apiKeyBatch));
	}
	
	public void readyBrowser() {
		setContentView(R.layout.install_browser);
		setWebView("file:///android_asset/v2/ok.html");

	}

	public void tourBrowser() {
		setContentView(R.layout.install_browser);
		setWebView("file:///android_asset/v2/index.html");
	}

	public void warningBrowser() {
		setContentView(R.layout.install_browser);
		setWebView("file:///android_asset/v2/error.html");
	}
	
	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface", "NewApi" })
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

}
