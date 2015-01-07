package com.prey.events.manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionsController;
import com.prey.events.Event;
import com.prey.json.parser.JSONParser;

public class SignalFlareRunner implements Runnable{

	private Context ctx=null;
	private Event event;
	
	public SignalFlareRunner(Context ctx,Event event){
		this.ctx=ctx;	
		this.event=event;
	}
	
	public void run() {
		try{
			if(event!=null){
				if ( Event.BATTERY_LOW.equals(event.getName())){
					PreyLogger.d("event.getName():"+event.getName());
					if (isValid()){
						String jsonString = "[ {\"command\": \"get\",\"target\": \"location\",\"options\": {}}]";
						List<JSONObject> jsonObjectList=new JSONParser().getJSONFromTxt(ctx, jsonString.toString());
						if (jsonObjectList!=null&&jsonObjectList.size()>0){
							ActionsController.getInstance(ctx).runActionJson(ctx,jsonObjectList);
						}
					}
				}
			}
		}catch(Exception e){
			
		}
	}

	private SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy hh:mm:ss",Locale.getDefault());
	
	public boolean isValid() {
		try{
			Calendar cal=Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR,-24);
			long leastSixHours=cal.getTimeInMillis();
			long signalFlareDate=PreyConfig.getPreyConfig(ctx).getSignalFlareDate();
			PreyLogger.d("signalFlareDate :"+signalFlareDate+" "+sdf.format(new Date(signalFlareDate)));
			PreyLogger.d("leastSixHours   :"+leastSixHours+" "+sdf.format(new Date(leastSixHours)));
			if(signalFlareDate==0||leastSixHours>signalFlareDate){
				long now=new Date().getTime();
				PreyConfig.getPreyConfig(ctx).setSignalFlareDate(now);
				return true;
			}
			return false;
		}catch(Exception e){
			return false;
		}
	}
	
}
