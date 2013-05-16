package com.prey.activities.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

import com.prey.PreyUtils;
import com.prey.R;
 
import com.prey.activities.browser.javascript.PreyJavaScriptInterface;
import com.prey.backwardcompatibility.FroyoSupport;


public class WizardBrowserActivity extends Activity {
	 private static final int SECURITY_PRIVILEGES = 10;
	
	 private WebView wizardBrowser=null;
     private boolean runOnce=false;
 
	 
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		
		@Override
		protected void onResume() {
			super.onResume();
			PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
			preyConfig.setActiveTour(false);
			
			 
			if (!runOnce){
				
				if (preyConfig.isActiveManager() &&preyConfig.isFroyoOrAbove() && !FroyoSupport.getInstance(this).isAdminActive()){
					PreyLogger.i("Is froyo or above!!");
					Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
					startActivityForResult(intent, SECURITY_PRIVILEGES);
					runOnce=true;
				} else {
					showScreen();
				}
			}else{
				preyConfig.setActiveManager(false);
				showScreen();
			} 
		}
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode == SECURITY_PRIVILEGES)
				showScreen();
		}
		
		@SuppressLint("SetJavaScriptEnabled")
	 private void showScreen(){
		 
			
	      
	        setContentView(R.layout.install_browser);
	      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	     //   this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

	        
	        PreyJavaScriptInterface myJavaScriptInterface=null;
	        myJavaScriptInterface= new PreyJavaScriptInterface(this,PreyUtils.getDeviceType(this));
	        
	        wizardBrowser = (WebView)findViewById(R.id.install_browser);
	        wizardBrowser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	        wizardBrowser.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");
	        wizardBrowser.getSettings().setJavaScriptEnabled(true);  
	        PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
	        
	    	wizardBrowser.loadUrl("file:///android_asset/final/wizard.html");
	    	 
	        	if (preyConfig.isFroyoOrAbove() && !FroyoSupport.getInstance(this).isAdminActive()){
	        		wizardBrowser.loadUrl("file:///android_asset/final/wizard2.html");
	        	}else{
	        		wizardBrowser.loadUrl("file:///android_asset/final/wizard.html");
	        	}
	         
	 }

		
		@Override
		protected void onPause() {
			
			super.onPause();
			
			PreyLogger.i("onPause");
		}
		
		
		@Override
	    protected void onStart(){
			super.onStart();
			PreyLogger.i("onStart");
		}
	    
	    protected void onRestart(){
	    	super.onRestart();
	    	
	    	
			PreyLogger.i("onRestart");
			Intent intent = new Intent(this, LoginBrowserActivity.class);
			startActivity(intent);
		}

	 
	 
	    protected void onStop(){
	    	super.onStop();
			PreyLogger.i("onStop");
		}

	    protected void onDestroy(){
	    	super.onDestroy();
			PreyLogger.i("onDestroy");
		}
	    
}


 
