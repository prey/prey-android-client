package com.prey.events.manager;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.events.Event;
import com.prey.events.retrieves.EventRetrieveDataBattery;
import com.prey.events.retrieves.EventRetrieveDataPrivateIp;
import com.prey.events.retrieves.EventRetrieveDataUptime;
import com.prey.events.retrieves.EventRetrieveDataWifi;
import com.prey.managers.PreyWifiManager;

public class EventManager {

	private EventMap<String, JSONObject> mapData = null;
	private Context ctx = null;
	public Event event = null;

	public final static String WIFI = "wifi";
	public final static String UPTIME = "uptime";
	public final static String PRIVATE_IP = "privateip";
	public final static String BATTERY = "battery";

	public EventManager(Context ctx) {
		this.ctx = ctx;
	}

	public void execute(Event event) {
		boolean isDeviceRegistered = isThisDeviceAlreadyRegisteredWithPrey(ctx);
		boolean isConnectionExists = false;
		boolean isOnline = false;
		
		String ssid=PreyWifiManager.getInstance(ctx).getSSID();
		
		
		
		String previousSsid=PreyConfig.getPreyConfig(ctx).getPreviousSsid();
		
		boolean validation=true;
		if (Event.WIFI_CHANGED.equals(event.getName())){
			if (ssid!=null&&!"".equals(ssid)&&!ssid.equals(previousSsid)&&!"<unknown ssid>".equals(ssid)&&!"0x".equals(ssid)){
				validation=true;
			}else{
				validation=false;
			}
		}
		
		 
		
		
		if (validation){
			PreyLogger.i("name:"+event.getName()+" info:"+event.getInfo()+" ssid["+ssid+"] previousSsid["+previousSsid+"]");
			PreyLogger.i("change PreviousSsid:"+ssid);
			PreyConfig.getPreyConfig(ctx).setPreviousSsid(ssid);
			try {
				isConnectionExists = PreyConfig.getPreyConfig(ctx).isConnectionExists();
				isOnline = PreyWifiManager.getInstance(ctx).isOnline();
			} catch (Exception e) {

			}
			// if This Device Already Registered With Prey
			if (isDeviceRegistered) {
				// if connection exists
				if (isConnectionExists) {
					// if there is connection, verify if online
					if (isOnline) {
						
						this.mapData = new EventMap<String, JSONObject>();
						this.event = event;
						this.mapData.put(EventManager.UPTIME, null);
						this.mapData.put(EventManager.WIFI, null);
						this.mapData.put(EventManager.PRIVATE_IP, null);
						this.mapData.put(EventManager.BATTERY, null);
						new EventRetrieveDataUptime().execute(ctx, this);
						new EventRetrieveDataWifi().execute(ctx, this);
						new EventRetrieveDataPrivateIp().execute(ctx, this);
						new EventRetrieveDataBattery().execute(ctx, this);
						
						//new Thread(new PreyBetaActionsRunner(ctx)).start();
						 
					}

				}
			}
		} 
		
		
	}
	
	
	
	

	public void receivesData(String key, JSONObject data) {
		mapData.put(key, data);
		if (mapData.isCompleteData()) {
			sendEvents();
		}
	}

	private void sendEvents() {
		if (mapData != null) {
			JSONObject jsonObjectStatus = mapData.toJSONObject();
			PreyLogger.d("jsonObjectStatus: " + jsonObjectStatus.toString());
			if (event != null) {
				if (PreyWifiManager.getInstance(ctx).isOnline()) {
					String lastEvent=PreyConfig.getPreyConfig(ctx).getLastEvent();
					if(!Event.WIFI_CHANGED.equals(event.getName()) || !event.getName().equals(lastEvent)){
						PreyConfig.getPreyConfig(ctx).setLastEvent(event.getName());
						PreyLogger.d("event name[" + this.event.getName() + "], info[" + this.event.getInfo() + "]");
						new EventThread(ctx, event, jsonObjectStatus).start();
					}
				}
			}
		}
	}





	private boolean isThisDeviceAlreadyRegisteredWithPrey(Context ctx) {
		return PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey();
	}

}
