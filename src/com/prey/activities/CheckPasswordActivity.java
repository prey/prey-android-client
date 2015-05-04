/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import java.util.Locale;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.backwardcompatibility.FroyoSupport;
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
		if (!FroyoSupport.getInstance(this).isAdminActive()) {
			String h1 = getString(R.string.device_not_ready_h1);
			String h2 = getString(R.string.device_not_ready_h2);
			TextView textH1 = (TextView) findViewById(R.id.device_ready_h1_text);
			TextView textH2 = (TextView) findViewById(R.id.device_ready_h2_text);
			textH1.setText(h1);
			textH2.setText(h2);
		}
		KeyboardStatusDetector keyboard = new KeyboardStatusDetector();
		keyboard.registerActivity(this); // or register to an activity
		keyboard.setVisibilityListener(new KeyboardVisibilityListener() {
			@Override
			public void onVisibilityChanged(boolean keyboardVisible) {
				ImageView logoImgTextPrey = (ImageView) findViewById(R.id.logo_img_reversed);
				ImageView logoImgExtra = (ImageView) findViewById(R.id.logo_img_extra);
				TextView textReady = (TextView) findViewById(R.id.device_ready_h1_text);
				TextView textForgotPassword= (TextView) findViewById(R.id.link_forgot_password);
				if (keyboardVisible) {
					PreyLogger.d("key on");
					if (logoImgTextPrey != null)
						logoImgTextPrey.setVisibility(View.GONE);
					if (logoImgExtra != null)
						logoImgExtra.setVisibility(View.GONE);
					if (textReady != null)
						textReady.setVisibility(View.GONE);
					if (textForgotPassword != null)
						textForgotPassword.setVisibility(View.GONE);
				} else {
					PreyLogger.d("key off");
					if (logoImgTextPrey != null)
						logoImgTextPrey.setVisibility(View.VISIBLE);
					if (logoImgExtra != null)
						logoImgExtra.setVisibility(View.VISIBLE);
					if (textReady != null)
						textReady.setVisibility(View.VISIBLE);
					if (textForgotPassword != null)
						textForgotPassword.setVisibility(View.VISIBLE);
				}
			}
		});
		
		
		try {
			TextView textForgotPassword= (TextView) findViewById(R.id.link_forgot_password);
			textForgotPassword.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					try {
						String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
						Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
						startActivity(browserIntent);
					} catch (Exception e) {
					}
					
				}  
			});
		} catch (Exception e) {
		}
		try {
			
			ImageView iconBack = (ImageView) findViewById(R.id.logo_img_reversed);
			if ("es".equals(Locale.getDefault().getLanguage())) {
				iconBack.setImageResource(R.drawable.icon_back_es);
			}
			RelativeLayout back=(RelativeLayout) findViewById(R.id.linear_back);
			back.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					Intent intent = null;
					intent = new Intent(getApplicationContext(), LoginActivity.class);
					startActivity(intent);
					finish();
					
				}  
			});
			
		} catch (Exception e) {
		}
		
	}

}
