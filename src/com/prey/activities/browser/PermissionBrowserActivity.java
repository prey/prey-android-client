package com.prey.activities.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.backwardcompatibility.FroyoSupport;

public class PermissionBrowserActivity extends Activity {
	private static final int SECURITY_PRIVILEGES = 10;

	private boolean runOnce = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PreyLogger.i("PermissionBrowserActivity onResume");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
	 
		if (!runOnce) {

			if ( preyConfig.isFroyoOrAbove() && !FroyoSupport.getInstance(this).isAdminActive()) {
				PreyLogger.i("Is froyo or above!!");
				Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
				startActivityForResult(intent, SECURITY_PRIVILEGES);
				runOnce = true;
			} else {
				showScreen();
			}
		} else {
			preyConfig.setActiveManager(false);
			showScreen();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SECURITY_PRIVILEGES){
			showScreen();
		
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void showScreen() {
		PreyLogger.i("PermissionBrowserActivity showScreen");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		Intent intent = null;
		if (preyConfig.isFroyoOrAbove() && !FroyoSupport.getInstance(this).isAdminActive()) {
			intent = new Intent(getApplicationContext(), WarningBrowserActivity.class);
		} else {
			intent = new Intent(getApplicationContext(), ReadyBrowserActivity.class);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

 
	 

	protected void onRestart() {
		super.onRestart();

		PreyLogger.i("PermissionBrowserActivity onRestart");
		Intent intent = new Intent(this, LoginBrowserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	 
	@Override
	public void onBackPressed() {
		PreyLogger.i("PermissionBrowserActivity onBackPressed");
		
	    return;
	}
	
}
