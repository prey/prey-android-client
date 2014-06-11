package com.prey.sms;

import com.prey.actions.sms.SMSFactory;

import android.content.Context;

public class SmsThread extends Thread {
	private Context ctx;

	private String messageSMS;
	private String phoneNumber;
	
    public SmsThread(Context ctx,String messageSMS,String phoneNumber) {
            this.messageSMS = messageSMS;
            this.ctx = ctx;
            this.phoneNumber=phoneNumber;
    }
    
    public void run() {
    	 SMSFactory.execute(ctx, messageSMS,phoneNumber);
    }
}
