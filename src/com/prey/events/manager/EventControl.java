package com.prey.events.manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.prey.PreyLogger;

public class EventControl {

	
	private static EventControl instance=null;
	private static Map<String,Long> map=null;
	
	private EventControl(){
		map=new HashMap<String, Long>();
	}
	
	public static EventControl getInstance(){
		if(instance==null){
			instance=new EventControl();
		} 
		return instance;
	}
	
	private SimpleDateFormat sdf2=new SimpleDateFormat("dd/MM/yy hh:mm:ss",Locale.getDefault());
	//private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault());
	public boolean valida(JSONObject json){
		
		
		String state="";
		String remaining="";
		
		try{
			
			 
		
			JSONObject jsonBattery=json.getJSONObject("battery_status");
			state=jsonBattery.getString("state");
			remaining=jsonBattery.getString("percentage_remaining");
			PreyLogger.d("state:"+state+" remaining:"+remaining);
		}catch(Exception e){
			
		}
		Date nowDate=new Date();
		long now = nowDate.getTime();
		if ("discharging".equals(state)||"stopped_charging".equals(state)){
			if(remaining!=null&&!"".equals(remaining)){
		 
				if(map.containsKey(state)){
					long time=map.get(state);
				
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.MINUTE, 2);
					long timeMoreTwo=cal.getTimeInMillis();
				
				
					PreyLogger.d("now        :"+now+" "+sdf2.format(new Date(now)));
					PreyLogger.d("timeMoreTwo:"+timeMoreTwo+" "+sdf2.format(new Date(timeMoreTwo)));
					if(timeMoreTwo>now){
						return false;
					}else{
						map.put(state,now);
						return true;
					}
				}else{
					map.put(state,now);
				    return true;
				}
			}else{
				return true;		
			}
		}else{
			return true;
		}
	
		
	}
	
	
}
