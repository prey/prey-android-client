package com.prey.twilio;

import java.util.HashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prey.PreyLogger;
import com.twilio.client.Connection;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;
import com.twilio.client.Twilio;
 

public class TwilioPhone implements Twilio.InitListener, DeviceListener
{
   
    private Context context;
    private Device device;
    private Connection connection;
    
    
    public TwilioPhone(Context context)
    {
    	this.context = context;
        Twilio.initialize(context, this /* Twilio.InitListener */);
    }
    
    public void connect()
    {
        connection = device.connect(null /* parameters */, null /* ConnectionListener */);
        if (connection == null)
        	PreyLogger.i("Failed to create new connection");
    }

    public void connect(String phoneNumber)
    {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("PhoneNumber", phoneNumber);
        connection = device.connect(parameters, null /* ConnectionListener */);
        if (connection == null)
            PreyLogger.i( "Failed to create new connection");
    }
    
    

    /* Twilio.InitListener method */
 
    public void onInitialized()
    {
    	PreyLogger.i( "Twilio SDK is ready");

 
        
        try {
            String clientName =  "oso";
            String capabilityToken = HttpHelper.httpGet("http://psicologiauc.cl/exalumnos/json/twilio/auth.php?clientName=" + clientName);
            device = Twilio.createDevice(capabilityToken, this);
            
 
            Intent intent = new Intent(context, TwilioPhoneActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            device.setIncomingIntent(pendingIntent);
        } catch (Exception e) {
            PreyLogger.i( "Failed to obtain capability token: " + e.getLocalizedMessage());
        }
        
        
    }
    
    public void handleIncomingConnection(Device inDevice, Connection inConnection)
    {
    	PreyLogger.i( "Device received incoming connection");
        if (connection != null)
            connection.disconnect();
        connection = inConnection;
        connection.accept();
    }
    

    /* Twilio.InitListener method */
     
    public void onError(Exception e)
    {
        PreyLogger.i( "Twilio SDK couldn't start: " + e.getLocalizedMessage());
    }

    
    public void disconnect()
    {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }
    
    @Override
    protected void finalize()
    {
    	if (connection != null){
            connection.disconnect();
    	}
        if (device != null)
            device.release();
    }

     /* DeviceListener method */
    public void onStartListening(Device inDevice)
    {
    	PreyLogger.i( "Device is now listening for incoming connections");
    }
 
      /* DeviceListener method */
    public void onStopListening(Device inDevice)
    {
    	PreyLogger.i( "Device is no longer listening for incoming connections");
    }
 
     /* DeviceListener method */
    public void onStopListening(Device inDevice, int inErrorCode, String inErrorMessage)
    {
    	PreyLogger.i( "Device is no longer listening for incoming connections due to error " +
                   inErrorCode + ": " + inErrorMessage);
    }
 
     /* DeviceListener method */
    public boolean receivePresenceEvents(Device inDevice)
    {
        return false;  // indicate we don't care about presence events
    }
 
      /* DeviceListener method */
    public void onPresenceChanged(Device inDevice, PresenceEvent inPresenceEvent) {
    	
    }
 

 

}
