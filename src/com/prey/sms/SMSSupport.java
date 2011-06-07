package com.prey.sms;

import java.util.ArrayList;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SMSSupport {
	
	public static ArrayList<String> getSMSMessage(Object[] pdus){
		ArrayList<String> smsMessages = new ArrayList<String>();
		SmsMessage[] msgs = null;
		msgs = new SmsMessage[pdus.length];
		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			// str += "SMS from " + msgs[i].getOriginatingAddress();
			// str += " :";
			// str += msgs[i].getMessageBody().toString();
			// str += "\n";
			try {
				smsMessages.add(msgs[i].getMessageBody().toString());
			} catch (Exception e) {
				// Nothing to do...
			}
		}
		return smsMessages;
	}

	public static void sendSMS(String destSMS, String message) {
		SmsManager sm = SmsManager.getDefault();
		sm.sendTextMessage(destSMS, null, message, null, null);
	}

}
