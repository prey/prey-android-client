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
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.prey.PreyConfig;
import com.prey.PreyController;
import com.prey.PreyLogger; 
import com.prey.actions.sms.SMSFactory;
import com.prey.actions.sms.SMSUtil;
import com.prey.sms.SmsThread;

public class SmsReceiver extends BroadcastReceiver {

	static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {
		// PreyLogger.d("SMS Broadcast - Action received: " +
		// intent.getAction());
		if (intent.getAction() != null && intent.getAction().equals(ACTION)) {
			// ---get the SMS message passed in---
			Bundle bundle = intent.getExtras();

			// String str = "";
			if (bundle != null) {
				// ---retrieve the SMS message received---
				Object[] pdus = (Object[]) bundle.get("pdus");
                String messageSMS ="";
                for (int i = 0; i < pdus.length; i++) {
               
                	SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                	String phoneNumber = currentMessage.getDisplayOriginatingAddress();
               
                	String senderNum = phoneNumber;
                	messageSMS = currentMessage.getDisplayMessageBody();
               
                	PreyLogger.i( "senderNum: "+ senderNum + "; message: " + messageSMS);
               
                	executeActionsBasedOnSMSMessage(context, messageSMS);
                	executeActions(context, messageSMS,phoneNumber);
               
                } // end for loop
			}
		}
	}

	private void executeActionsBasedOnSMSMessage(Context ctx, String messageSMS) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		PreyLogger.i("SMS received: " + messageSMS);
		boolean shouldPerform = messageSMS.indexOf(preyConfig.getSmsToRun()) >= 0;
		boolean shouldStop = messageSMS.indexOf(preyConfig.getSmsToStop()) >= 0;
		if (shouldPerform) {
			PreyLogger.i("SMS Match!, waking up Prey right now!");
			abortBroadcast(); //To remove the SMS from the inbox
			PreyController.startPrey(ctx);
		} else {
			if (shouldStop) {
				PreyLogger.i("SMS Match!, stopping Prey!");
				abortBroadcast(); //To remove the SMS from the inbox
				PreyController.stopPrey(ctx);
			}
		}
	}
	
	
	private void executeActions(Context ctx, String messageSMS,String phoneNumber) {
		 if (SMSUtil.isValidSMSCommand(messageSMS)){
             this.abortBroadcast();
             new SmsThread(ctx,messageSMS,phoneNumber).start();
            
		 }	
	}

}
