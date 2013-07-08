package com.prey.activities.preferences;

import com.prey.R;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
 

public class PreferenceSecurityActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_security);
       // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
	protected void onResume() {
		super.onResume();
	 	Intent intent = new Intent(PreferenceSecurityActivity.this, PreyConfigurationSecurityActivity.class);
		startActivity(intent);
		finish();		
 
	} 
}
