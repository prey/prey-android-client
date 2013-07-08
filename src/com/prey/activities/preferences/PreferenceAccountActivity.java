package com.prey.activities.preferences;

import com.prey.R;
 

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
 

public class PreferenceAccountActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_account);
    }

    @Override
	protected void onResume() {
		super.onResume();
	 	Intent intent = new Intent(PreferenceAccountActivity.this, PreyConfigurationAccountActivity.class);
		startActivity(intent);
		finish();		
 
	} 
}
