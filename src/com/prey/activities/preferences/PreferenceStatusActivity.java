package com.prey.activities.preferences;

import com.prey.R;
 

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
 

public class PreferenceStatusActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_status);
    }

    @Override
	protected void onResume() {
		super.onResume();
	 	Intent intent = new Intent(PreferenceStatusActivity.this, PreyConfigurationStatusActivity.class);
		startActivity(intent);
		finish();		
 
	} 
}
