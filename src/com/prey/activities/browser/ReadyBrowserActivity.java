package com.prey.activities.browser;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.R;


public class ReadyBrowserActivity extends BaseBrowserActivity {

	WebView installBrowser = null;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.install_browser);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setActiveWizard(false);
		
	 
		WebView installBrowser = getWebView();
		//installBrowser.loadUrl("file:///android_asset/final/ready.html");
		installBrowser.loadUrl("file:///android_asset/v1/index.html#ok");
	}

 
}
