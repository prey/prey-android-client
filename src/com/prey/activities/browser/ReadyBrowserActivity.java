package com.prey.activities.browser;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;

import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;


public class ReadyBrowserActivity extends BaseBrowserActivity {

	WebView installBrowser = null;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreyLogger.i("ReadyBrowserActivity onCreate");
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.install_browser);
		

		
		
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setActiveWizard(false);
		
	 
		WebView installBrowser = getWebView();
		//installBrowser.loadUrl("file:///android_asset/final/ready.html");
		String url="file:///android_asset/v1/ok.html";
		PreyLogger.i("url:"+url);
		installBrowser.loadUrl(url);
	}
	
	@Override
	public void onDestroy() {
	        super.onDestroy();
	        PreyLogger.i("ReadyBrowserActivity onDestroy");
	      
	 }
	
	
	@Override
	public void onPause() {
	        super.onPause();
	        PreyLogger.i("ReadyBrowserActivity onPause");
	        this.finish();
	 }
	
	@Override
	public void onStop() {
	        super.onStop();
	        PreyLogger.i("ReadyBrowserActivity onStop");
	        finish();
	 }
	
	 @Override
	 protected void onResume() {
	     super.onResume();
	     PreyLogger.i("ReadyBrowserActivity onResume");
	 }
	 
		@Override
		public void onBackPressed() {
			PreyLogger.i("ReadyBrowserActivity onBackPressed");
		    
			onBackPressed();
		}
}
