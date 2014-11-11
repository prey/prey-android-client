/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.util.KeyboardStatusDetector;
import com.prey.util.KeyboardVisibilityListener;

public class CheckPasswordActivity extends PasswordActivity {

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		updateLoginScreen();
		bindPasswordControls();
		
		KeyboardStatusDetector keyboard=new KeyboardStatusDetector();
        
		keyboard.registerActivity(this); //or register to an activity
        keyboard.setVisibilityListener(new KeyboardVisibilityListener() {
			
			@Override
			public void onVisibilityChanged(boolean keyboardVisible) {
		        ImageView logoImg=(ImageView) findViewById(R.id.logo_img_password);
				if(keyboardVisible) {
                    PreyLogger.i("key on");
                    if(logoImg!=null)
                    	logoImg.setVisibility(View.GONE);
                 }else {
                	 PreyLogger.i("key off");
                	 if(logoImg!=null)
                		 logoImg.setVisibility(View.VISIBLE);
                 }
				
			}
		});
	}

}
