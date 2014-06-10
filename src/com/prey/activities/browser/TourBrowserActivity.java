package com.prey.activities.browser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

public class TourBrowserActivity extends BaseBrowserActivity {

	WebView installBrowser = null;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createEnvironment();
		WebView installBrowser = getWebView();
		installBrowser.loadUrl("file:///android_asset/v2/index.html");
	}

}
