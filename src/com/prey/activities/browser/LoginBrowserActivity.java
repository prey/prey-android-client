package com.prey.activities.browser;

import android.os.Bundle;
import android.webkit.WebView;


public class LoginBrowserActivity extends BaseBrowserActivity {

	WebView installBrowser = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createEnvironment();
		WebView installBrowser = getWebView();
		installBrowser.loadUrl("file:///android_asset/v2/index.html");
	}

}
