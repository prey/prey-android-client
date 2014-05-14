/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;
 
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.prey.FileConfigReader;
import com.prey.PreyConfig; 
import com.prey.PreyLogger;
import com.prey.PreyUtils; 
import com.prey.beta.actions.PreyBetaController;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
 

public class C2DMReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
			handleRegistration(context, intent);
		} else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
			handleMessage(context, intent);
		}
	}

	private void handleMessage(Context context, Intent intent) {
		PreyLogger.d("PUSH_______________");
		PreyConfig config=PreyConfig.getPreyConfig(context);
		String pushedMessage = intent.getExtras().getString(config.getc2dmAction());
		 
		String api_key=intent.getExtras().getString("api_key");
		String remote_email=intent.getExtras().getString("remote_email");
		 
		if((api_key!=null&&!"".equals(api_key))||(pushedMessage!=null&&pushedMessage.indexOf("api_key")>0)){
			registrationPlanB(context,api_key,remote_email, pushedMessage);
		}else{
			handleMessageBeta(context, pushedMessage);
			config.setVersion(PreyConfig.VERSION_V2);
			config.setMissing(false);
		}
	}
	
	private void registrationPlanB(Context context, String apiKey, String remoteEmail,String pushedMessage){
		if(apiKey==null){
			try{
				JSONObject jsnobject = new JSONObject(pushedMessage);
				apiKey=jsnobject.getString("api_key");
				remoteEmail=jsnobject.getString("remote_email");
			}catch(Exception e){
				
			}
		}
		if(apiKey!=null){
			try {
				PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(context, apiKey, remoteEmail, PreyUtils.getDeviceType(context));
			} catch (PreyException e) {
				PreyLogger.e("Error, causa:"+e.getMessage(), e);
			}
		}
		 
	}
	
	private void handleMessageBeta(Context context, String pushedMessage) {
	    PreyLogger.i("Push notification received, waking up Prey right now!");
		PreyLogger.i("Push message received " + pushedMessage);
		startPrey(context);
	}
	
	private void startPrey(Context context) {
	    try {
			PreyBetaController.startPrey(context);
		} catch (Exception e) {
			PreyLogger.e("Push execution failed to run", e);
		}
	}

	private void handleRegistration(Context context, Intent intent) {
		String registration = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null) {
			PreyLogger.d("Couldn't register to c2dm: " + intent.getStringExtra("error"));
			PreyConfig.getPreyConfig(context).setRegisterC2dm(false);
		} else if (intent.getStringExtra("unregistered") != null) {
			// unregistration done, new messages from the authorized sender will
			// be rejected
			PreyLogger.d("Unregistered from c2dm: " + intent.getStringExtra("unregistered"));
			PreyConfig.getPreyConfig(context).setRegisterC2dm(false);
			PreyConfig.getPreyConfig(context).setNotificationId("");
		} else if (registration != null) {
			//PreyLogger.d("Registration id: " + registration);
			new UpdateCD2MId().execute(registration, context);
			
			// Send the registration ID to the 3rd party site that is sending
			// the messages.
			// This should be done in a separate thread.
			// When done, remember that all registration is done.
		}
	}

	private class UpdateCD2MId extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... data) {
			try {
				Context ctx=(Context) data[1];
				String registration = FileConfigReader.getInstance(ctx).getGcmIdPrefix() + (String) data[0];
				PreyHttpResponse response=PreyWebServices.getInstance().setPushRegistrationId(ctx, registration);
				PreyConfig.getPreyConfig(ctx).setNotificationId(registration);
				PreyLogger.d("response:"+response.toString());
				PreyConfig.getPreyConfig(ctx).setRegisterC2dm(true);
				startPrey(ctx);
			} catch (Exception e) {
				PreyLogger.e("Failed registering to CD2M: " + e.getLocalizedMessage(), e);
			}
			return null;
		}
	}

}
