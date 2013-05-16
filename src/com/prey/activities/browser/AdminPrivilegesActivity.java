package com.prey.activities.browser;


import com.prey.PreyLogger;
import com.prey.backwardcompatibility.FroyoSupport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AdminPrivilegesActivity extends Activity {
	
	private static final int SECURITY_PRIVILEGES = 10;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		PreyLogger.i("Is froyo or above!!");
		Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
		startActivityForResult(intent, SECURITY_PRIVILEGES);	
	
	}

}
