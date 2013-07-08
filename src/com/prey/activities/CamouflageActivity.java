package com.prey.activities;

import com.prey.PreyLogger;

import com.prey.activities.browser.BaseBrowserActivity;

 
import android.os.Bundle;
import android.webkit.WebView;

public class CamouflageActivity /*extends Activity {

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camouflage2);
	}
	
	*/
	extends BaseBrowserActivity {

		WebView installBrowser = null;
		 
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			createEnvironment();
			WebView installBrowser = getWebView();	
			installBrowser.loadUrl("file:///android_asset/games/seals.html");
			this.playAudio();
		}

		@Override
		protected void onPause() {
			super.onPause();
			PreyLogger.i("onPause");
			stopAudio();
			this.finish();
		}

		@Override
		protected void onStart() {
			super.onStart();
			PreyLogger.i("onStart");
			this.playAudio();
		}

		protected void onRestart() {
			super.onRestart();
			PreyLogger.i("onRestart");
			stopAudio();
			this.finish();
		}

		protected void onStop() {
			super.onStop();
			PreyLogger.i("onStop");
			stopAudio();
			this.finish();
		}

		protected void onDestroy() {
			super.onDestroy();
			PreyLogger.i("onDestroy");
			stopAudio();
			this.finish();
		}
		
		 
		public void onBackPressed() {
		 
		super.onBackPressed();
		stopAudio();
		this.finish();
		}
		
		private  void playAudio(){
	           
	           
 
	           
	    }
		private  void stopAudio(){
	           
	           
	             
 
	             
	    }
}

