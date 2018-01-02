/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.backwardcompatibility;

import android.telephony.gsm.SmsManager;
import android.telephony.gsm.SmsMessage;

import java.util.ArrayList;

public class CupcakeSupport {

    public static ArrayList<String> getSMSMessage(Object[] pdus) {
        ArrayList<String> smsMessages = new ArrayList<String>();
        SmsMessage[] msgs = null;
        msgs = new SmsMessage[pdus.length];
        for (int i = 0; i < msgs.length; i++) {
            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            // str += "SMS from " + msgs[i].getOriginatingAddress();
            // str += " :";
            // str += msgs[i].getMessageBody().toString();
            // str += "\n";
            smsMessages.add(msgs[i].getMessageBody());
        }
        return smsMessages;
    }

    public static void sendSMS(String destSMS, String message) {
        SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage(destSMS, null, message, null, null);

    }

}
