package com.prey.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.browser.javascript.PreyJavaScriptInterface;

public class WarningActivity extends Activity {

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Delete notifications (in case Activity was started by one of them)
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		startup();

	}

	@Override
	protected void onStart() {
		super.onStart();
		startup();
	}

	private void startup() {
		createEnvironment();
		WebView installBrowser = getWebView();
		installBrowser.loadUrl("file:///android_asset/v2/error.html");

	}

	public void createEnvironment() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//
		setContentView(R.layout.install_browser);

	}

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	public WebView getWebView() {
		WebView installBrowser = (WebView) findViewById(R.id.install_browser);
		installBrowser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		PreyJavaScriptInterface myJavaScriptInterface = null;
		myJavaScriptInterface = new PreyJavaScriptInterface(this, PreyUtils.getDeviceType(this));
		installBrowser.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");
		installBrowser.getSettings().setJavaScriptEnabled(true);
		return installBrowser;
	}

	@SuppressLint("NewApi")
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}
