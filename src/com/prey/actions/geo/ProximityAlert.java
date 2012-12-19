package com.prey.actions.geo;

import com.prey.PreyLogger;

import android.app.Activity;
import android.os.Bundle;

public class ProximityAlert extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		PreyLogger.i("intent Hi");
		finish();
	}

}
