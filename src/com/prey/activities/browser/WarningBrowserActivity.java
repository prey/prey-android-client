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
		installBrowser.loadUrl("file:///android_asset/v1/warning.html");
	}

	@Override
	protected void onPause() {

		super.onPause();

		PreyLogger.i("WarningBrowserActivity onPause");
		this.finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		PreyLogger.i("WarningBrowserActivity onStart");
	}

	protected void onRestart() {
		super.onRestart();
		PreyLogger.i("WarningBrowserActivity onRestart");
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	protected void onStop() {
		super.onStop();
		PreyLogger.i("WarningBrowserActivity onStop");
		 finish();
	}

	protected void onDestroy() {
		super.onDestroy();
		PreyLogger.i("WarningBrowserActivity onDestroy");
		 
	}
	
	 @Override
	 protected void onResume() {
	     super.onResume();
	     PreyLogger.i("WarningBrowserActivity onResume");
	 }
	 
	@Override
	public void onBackPressed() {
		PreyLogger.i("WarningBrowserActivity onBackPressed");
		//  this.finish();
	    
	    return;
	}
}
