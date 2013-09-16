package com.prey.twilio;

import android.content.Context;

 

public class TwilioRunner implements Runnable {

	
	private Context ctx = null;
 

	public TwilioRunner(Context ctx ) {
		this.ctx = ctx;
 
	}
	
	public void run() {
		TwilioPhoneManager.getInstance(ctx);		
	}

}
