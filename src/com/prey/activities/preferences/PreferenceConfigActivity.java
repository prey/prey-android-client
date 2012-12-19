package com.prey.activities.preferences;


import com.prey.R;


import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Intent;
 

public class PreferenceConfigActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_config);
    }
 
    
    @Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(PreferenceConfigActivity.this, PreyConfigurationConfigActivity.class);
		startActivity(intent);
		finish();		
	} 
}
