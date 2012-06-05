package com.prey.activities;

import com.prey.PreyApp;
import com.prey.PreyConfig;

import android.app.Activity;
import android.content.Intent;

public class PreyActivity extends Activity {
	
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

}
