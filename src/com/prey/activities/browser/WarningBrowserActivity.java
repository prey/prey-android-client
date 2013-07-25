package com.prey.activities.browser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
 
import android.webkit.WebView;

 
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.activities.LoginActivity;

public class WarningBrowserActivity extends BaseBrowserActivity {

	WebView installBrowser = null;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setActiveWizard(false);
		
		createEnvironment();
		WebView installBrowser = getWebView();
		//installBrowser.loadUrl("file:///android_asset/final/warning.html");
		installBrowser.loadUrl("file:///android_asset/v1/index.html#error");
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
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
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
