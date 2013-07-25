package com.prey.activities.browser;


import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.browser.javascript.PreyJavaScriptInterface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.webkit.WebView;


public class BaseBrowserActivity  extends Activity implements SurfaceHolder.Callback {

	public static SurfaceView mSurfaceView;
	public static SurfaceHolder mSurfaceHolder;
	public static Camera mCamera;
	
	public static final String PAGE_URL="pageUrl";
	
	public void onConfigurationChanged(Configuration newConfig){
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	public void createEnvironment(){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.install_browser);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
	
 
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

 
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

 
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}
}
