package com.prey.twilio;

import com.twilio.client.Connection;
import com.twilio.client.Device;

import android.content.Context;
import android.content.Intent;

public class TwilioPhoneManager {

	
	private static TwilioPhoneManager instance=null;
	 
	private TwilioPhone phone;
	
	private TwilioPhoneManager(){
		
	}
	private TwilioPhoneManager(Context context){
		initialize(context);
	}
	
	public static TwilioPhoneManager getInstance(Context context){
		if(instance==null){
			instance=new TwilioPhoneManager(context);
		}
		return instance;
	}
	
	
	public void initialize(Context context){
		phone = new TwilioPhone(context);
	}
	
	public void handleIncomingConnection(Intent intent){
        Device device = intent.getParcelableExtra(Device.EXTRA_DEVICE);
        Connection connection = intent.getParcelableExtra(Device.EXTRA_CONNECTION);
        if (device != null && connection != null) {
            intent.removeExtra(Device.EXTRA_DEVICE);
            intent.removeExtra(Device.EXTRA_CONNECTION);
            phone.handleIncomingConnection(device, connection);
        }
	}
	
	
	public void connect(){
		 phone.connect();
	}
	
	public void disconnect(){
		 phone.disconnect();
	}
}
