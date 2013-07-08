package com.prey.activities.browser;

import android.os.Bundle;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

public class PrePermissionBrowserActivity extends BaseBrowserActivity {

	WebView installBrowser = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createEnvironment();
		WebView installBrowser = getWebView();
		//installBrowser.loadUrl("file:///android_asset/final/permission.html");
		installBrowser.loadUrl("file:///android_asset/v1/index.html#error");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setActiveManager(true);
		
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
