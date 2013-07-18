package com.prey.activities.browser;

import android.annotation.SuppressLint;

import android.os.Bundle;
import android.webkit.WebView;

import com.prey.PreyConfig;


public class ReadyBrowserActivity extends BaseBrowserActivity {

	WebView installBrowser = null;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setActiveWizard(false);
		
		createEnvironment();
		WebView installBrowser = getWebView();
		//installBrowser.loadUrl("file:///android_asset/final/ready.html");
		installBrowser.loadUrl("file:///android_asset/v1/index.html#ok");
	}

 
}
