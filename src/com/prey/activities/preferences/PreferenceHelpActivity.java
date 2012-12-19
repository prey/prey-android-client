package com.prey.activities.preferences;

import com.prey.R;
 

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
 

public class PreferenceHelpActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_help);
    }

    @Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(PreferenceHelpActivity.this, PreyConfigurationHelpActivity.class);
		startActivity(intent);
		finish();		
	} 
}
