package com.prey.activities;

import com.prey.PreyConfig;
import com.prey.R;

import android.app.Activity;
import android.os.Bundle;

public class CongratulationsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreyConfig.getPreyConfig(CongratulationsActivity.this).registerC2dm();
		setContentView(R.layout.congratulations);
	}

}
