package com.prey.install;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

public class PreyInstallRemoteReceiver  extends BroadcastReceiver {

	private SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy hh:mm:ss");
    @Override
    public void onReceive(Context ctx, Intent intent) {
        PreyLogger.i("Broadcast - Action received: "+intent.getAction());
        PreyConfig preyConfig=PreyConfig.getPreyConfig(ctx);
        preyConfig.registerC2dm();
        
        boolean configured=preyConfig.isThisDeviceAlreadyRegisteredWithPrey(false);
        if (configured){
        	//sleep
        }else{
        	if (preyConfig.isSendNotificationId() || !lastExecutionminutes(ctx)){
		  		//sleep();
        		//PreyLogger.i("____install");
		  	}else{
		  		registerDevice(ctx);// # sends device activation email
		  	}
        }
    }

    public void registerDevice(Context ctx){
    	new PreyInstallRemoteThread(ctx).start();
    
	}
    
    public Date timeNow(){
		return new Date();
	}
    
	public boolean lastExecutionminutes(Context ctx){
		Calendar cal=Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE,-1);
		long leastTwoHours=cal.getTimeInMillis();
		long installtionDate=PreyConfig.getPreyConfig(ctx).getInstallationDate();
		PreyLogger.i("installtionDate:"+installtionDate+" "+sdf.format(new Date(installtionDate)));
		PreyLogger.i("leastTwoHours:"+leastTwoHours+" "+sdf.format(new Date(leastTwoHours)));
		if(leastTwoHours>installtionDate){
				return true;
		}
		return false;
	}
}
