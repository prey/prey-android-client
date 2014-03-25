package com.prey.events.manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionsController;
import com.prey.events.Event;
import com.prey.json.parser.JSONParser;
import com.prey.net.PreyWebServices;

public class SignalFlareRunner implements Runnable{

	private Context ctx=null;
	private Event event;
	
	public SignalFlareRunner(Context ctx,Event event){
		this.ctx=ctx;	
		this.event=event;
	}
	
	public void run() {
		if(event!=null){
			if ( Event.BATTERY_LOW.equals(event.getName())){
				if (isValid()){
					String jsonString = "[ {\"command\": \"get\",\"target\": \"location\",\"options\": {}}]";
					List<JSONObject> jsonObjectList=new JSONParser().getJSONFromTxt(ctx, jsonString.toString());
					if (jsonObjectList!=null&&jsonObjectList.size()>0){
						ArrayList<HttpDataService> dataToBeSent=(ArrayList<HttpDataService>)ActionsController.getInstance(ctx).runActionJson(ctx,jsonObjectList);
						PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
					}
				}
			}
		}
	}

	private SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy hh:mm:ss");
	public boolean isValid() {
		Calendar cal=Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE,-10);
		long leastTenMinutes=cal.getTimeInMillis();
		long signalFlareDate=PreyConfig.getPreyConfig(ctx).getSignalFlareDate();
		PreyLogger.d("signalFlareDate :"+signalFlareDate+" "+sdf.format(new Date(signalFlareDate)));
		PreyLogger.d("leastFiveMinutes:"+leastTenMinutes+" "+sdf.format(new Date(leastTenMinutes)));
		if(signalFlareDate==0||leastTenMinutes>signalFlareDate){
			long now=new Date().getTime();
			PreyConfig.getPreyConfig(ctx).setSignalFlareDate(now);
			return true;
		}
		return false;
	}
	
}
