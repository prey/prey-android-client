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
 
 
import com.prey.PreyLogger;
 
import com.prey.actions.sms.SMSFactory;
import com.prey.actions.sms.SMSUtil;
 

public class SmsReceiver extends BroadcastReceiver {

	static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	public void onReceive(Context context, Intent intent) {
		PreyLogger.i("SMS Broadcast - Action received: " + intent.getAction());
		if (intent.getAction() != null && intent.getAction().equals(ACTION)) {

			
			final Bundle bundle = intent.getExtras();
			 
			try {
			     
			    if (bundle != null) {
			         
			        final Object[] pdusObj = (Object[]) bundle.get("pdus");
			        String message ="";
			        for (int i = 0; i < pdusObj.length; i++) {
			             
			            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
			            String phoneNumber = currentMessage.getDisplayOriginatingAddress();
			             
			            String senderNum = phoneNumber;
			            message = currentMessage.getDisplayMessageBody();
			 
			            PreyLogger.i( "senderNum: "+ senderNum + "; message: " + message);
			             
			  
			         
			             
			        } // end for loop
			        if (SMSUtil.isValidSMSCommand(message)){
			        	this.abortBroadcast();
			        	SMSFactory.execute(context, message);
			        }
			      } // bundle is null
			 
			} catch (Exception e) {
				PreyLogger.e(  "Exception smsReceiver" +e.getMessage(),e);
			     
			}
		}
	}
	

}
