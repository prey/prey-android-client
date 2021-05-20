/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
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
import com.prey.PreyLogger;
import com.prey.actions.sms.SMSUtil;
import com.prey.actions.sms.SmsThread;

public class SmsReceiver extends BroadcastReceiver {

    static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    static final String SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && (intent.getAction().equals(SMS_RECEIVED)||intent.getAction().equals(SMS_DELIVER))) {
            // ---get the SMS message passed in---
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // ---retrieve the SMS message received---
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String messageBody = currentMessage.getMessageBody();
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String displayMessageBody = currentMessage.getDisplayMessageBody();
                    PreyLogger.d(String.format("senderNum: %s message:%s",phoneNumber,displayMessageBody));
                    executeActions(context, messageBody,displayMessageBody, phoneNumber);
                } // end for loop
            }
        }
    }

    private void executeActions(Context ctx, String messageBody,String displayMessageBody, String phoneNumber) {
        String pinNumber=PreyConfig.getPreyConfig(ctx).getPinNumber();
        if(pinNumber!=null&&!"".equals(pinNumber)){
            if (SMSUtil.isValidSMSCommand(displayMessageBody)) {
                this.abortBroadcast();
                new SmsThread(ctx, displayMessageBody, phoneNumber).start();
            }
        }
    }

}