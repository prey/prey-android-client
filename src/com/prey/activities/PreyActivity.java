/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.prey.PreyConfig;
import com.prey.analytics.GoogleAnalyticsSessionManager;

public class PreyActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Need to do this for every activity that uses google analytics
        GoogleAnalyticsSessionManager.getInstance(getApplication()).incrementActivityCount();
	}
	

    @Override
    protected void onResume() {
        super.onResume();

        // Example of how to track a pageview event
        GoogleAnalyticsTracker.getInstance().trackPageView(getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Purge analytics so they don't hold references to this activity
        GoogleAnalyticsTracker.getInstance().dispatch();

        // Need to do this for every activity that uses google analytics
        GoogleAnalyticsSessionManager.getInstance().decrementActivityCount();
    }
	
	/*
	@Override
	public void onPause() {
	    super.onPause();
	    ((PreyApp)this.getApplication()).mLastPause = System.currentTimeMillis();
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    PreyApp app = ((PreyApp)this.getApplication());
	    if (System.currentTimeMillis() - app.mLastPause > PreyConfig.PASSWORD_PROMPT_DELAY) {
	    	Intent intent = new Intent(PreyActivity.this, CheckPasswordActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
	    }
	}
	*/
	
	protected PreyConfig getPreyConfig(){
		return PreyConfig.getPreyConfig(PreyActivity.this);
	}


	
}
