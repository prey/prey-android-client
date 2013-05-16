package com.prey.activities.browser;


import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.browser.javascript.PreyJavaScriptInterface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.Window;
import android.webkit.WebView;


public class BaseBrowserActivity  extends Activity {

	public static final String PAGE_URL="pageUrl";
	
	public void createEnvironment(){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.install_browser);
	}
	
	@SuppressLint("SetJavaScriptEnabled") 
	public WebView getWebView(){
		WebView installBrowser = (WebView) findViewById(R.id.install_browser);
		installBrowser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		PreyJavaScriptInterface myJavaScriptInterface = null;
		myJavaScriptInterface = new PreyJavaScriptInterface(this, PreyUtils.getDeviceType(this));
		installBrowser.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");
		installBrowser.getSettings().setJavaScriptEnabled(true);
		return installBrowser;
	}
}
