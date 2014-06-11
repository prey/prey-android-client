package com.prey.actions.geo;

import com.prey.PreyLogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class LocationAlertReceiver  extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		PreyLogger.d("Received Intent!");
		String date = intent.getStringExtra("date");
		String locationName = intent.getStringExtra("locationName");
		String latitude=intent.getStringExtra("latitude" );
		String longitude=intent.getStringExtra("longitude");
		String radius=intent.getStringExtra("radius");
		boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
		StringBuffer msg=new StringBuffer("");
		msg.append("LocationAlert: ");
		msg.append("locationName:").append(locationName).append(" ");
		msg.append("date: ").append(date).append(" ");
		msg.append("latitude: ").append(latitude).append(" ");
		msg.append("longitude: ").append(longitude).append(" ");
		msg.append("radius: ").append(radius).append(" ");
		msg.append("|enter: ").append( isEntering);
		PreyLogger.d(msg.toString());
		Toast.makeText(ctx, msg.toString(),Toast.LENGTH_LONG).show();
	}

}
