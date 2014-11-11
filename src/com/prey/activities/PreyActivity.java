/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

import com.prey.PreyConfig;
import com.prey.PreyUtils;

public class PreyActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
	}
	

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


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

	protected String getDeviceType() {
		return PreyUtils.getDeviceType(this);
	}

}
