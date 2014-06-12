package com.prey.activities.browser;

import android.os.Bundle;
import android.webkit.WebView;

import com.prey.PreyLogger;

public class UserRegisteredBrowserActivity extends BaseBrowserActivity {

	WebView installBrowser = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createEnvironment();
		WebView installBrowser = getWebView();
		installBrowser.loadUrl("file:///android_asset/final/userRegistered.html");
	}

	@Override
	protected void onPause() {
		super.onPause();
		PreyLogger.i("onPause");
	}

	@Override
	protected void onStart() {
		super.onStart();
		PreyLogger.i("onStart");
	}

	protected void onRestart() {
		super.onRestart();
		PreyLogger.i("onRestart");
	}

	protected void onStop() {
		super.onStop();
		PreyLogger.i("onStop");
	}

	protected void onDestroy() {
		super.onDestroy();
		PreyLogger.i("onDestroy");
	}

}
