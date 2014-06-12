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
		installBrowser.loadUrl("file:///android_asset/v2/index.html#error");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setActiveManager(true);
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		PreyLogger.i("PrePermissionBrowserActivity onPause");
	}

	@Override
	protected void onStart() {
		super.onStart();
		PreyLogger.i("PrePermissionBrowserActivity onStart");
	}

	protected void onRestart() {
		super.onRestart();
		PreyLogger.i("PrePermissionBrowserActivity onRestart");
	}

	protected void onStop() {
		super.onStop();
		PreyLogger.i("PrePermissionBrowserActivity onStop");
	}

	protected void onDestroy() {
		super.onDestroy();
		PreyLogger.i("PrePermissionBrowserActivity onDestroy");
	}
	
	@Override
	public void onBackPressed() {
		PreyLogger.i("PrePermissionBrowserActivity onBackPressed");
		
	    return;
	}
}
