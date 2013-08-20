/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyController;
import com.prey.PreyLogger;
import com.prey.net.PreyWebServices;


public class C2DMReceiver extends BroadcastReceiver {
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
	        handleRegistration(context, intent);
	    } else {
	    	if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
	    		handleMessage(context, intent);
	    	}
	    }
	 }

	private void handleMessage(Context context, Intent intent) {
	    try {
			PreyLogger.i("Push notification received, waking up Prey right now!");
			PreyController.startPrey(context);
		} catch (Exception e) {
			PreyLogger.e("Push execution failed to run", e);
		}
	}
	

	private void handleRegistration(Context context, Intent intent) {
	    String registrationId = intent.getStringExtra("registration_id"); 
	    PreyConfig.getPreyConfig(context).setNotificationId(registrationId);
	    if (intent.getStringExtra("error") != null) {
	    	PreyLogger.i("Couldn't register to c2dm: " + intent.getStringExtra("error"));
	    	
	    } else if (intent.getStringExtra("unregistered") != null) {
	        // unregistration done, new messages from the authorized sender will be rejected
	    	PreyLogger.i("Unregistered from c2dm: " + intent.getStringExtra("unregistered"));
	    } else if (registrationId != null) {
	    	PreyLogger.i("Registration id: " + registrationId);
	    	PreyConfig.getPreyConfig(context).setRegistrationId(registrationId);
	    	
	    	try{
	    		new UpdateCD2MId().execute(registrationId, context);
	    	}catch(Exception e){
	    		
	    	}
	       // Send the registration ID to the 3rd party site that is sending the messages.
	       // This should be done in a separate thread.
	       // When done, remember that all registration is done. 
	    }else{
	    	Toast.makeText(context, "registration_id nulo" , Toast.LENGTH_LONG).show();
	    }
	}
	
	private class UpdateCD2MId extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... data) {
			try {
				String registration = FileConfigReader.getInstance((Context)data[1]).getGcmIdPrefix()+(String)data[0];
				PreyLogger.i("Registration id: " + registration);
		    	PreyWebServices.getInstance().setPushRegistrationId((Context)data[1], registration);
		    	
			} catch (Exception e) {
				
				PreyLogger.e("Failed registering to CD2M: " + e.getLocalizedMessage(),e);
			}
			return null;
		}

	}

}
