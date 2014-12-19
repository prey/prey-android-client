package com.prey.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;


 
import com.prey.R;
 

public class BatteryLevelActivity extends Activity {
 

	private static final int SHOW_POPUP = 0;
	private String message = null;
	
    @Override
    /**
     * Called when the current activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		batteryLevel();
		showDialog(SHOW_POPUP);
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {

		Dialog popup = null;
		switch (id) {

		case SHOW_POPUP:
			popup = new AlertDialog.Builder(BatteryLevelActivity.this).setIcon(R.drawable.logo).setTitle(R.string.popup_alert_title).setMessage(this.message)
					.setCancelable(true).create();

			popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {

					
					finish();
				}
			});
		}
		return popup;
	}

    /**
     * Computes the battery level by registering a receiver to the intent triggered 
     * by a battery status/level change.
     */
    private void batteryLevel() {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                int level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                message="Battery Level Remaining: " + level + "%" ;
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }

}
