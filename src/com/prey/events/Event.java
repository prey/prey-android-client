package com.prey.events;

import org.json.JSONObject;

public class Event {
	
	public static final String SIM_CHANGED="sim_changed";
	public static final String WIFI_CHANGED="ssid_changed";
	public static final String PLUGGED="started_charging";
	public static final String UN_PLUGGED="stopped_charging";
	public static final String TURNED_ON="device_turned_on";
	public static final String TURNED_OFF="device_turned_off";
	public static final String BATTERY_LOW="low_battery";
	public static final String APPLICATION_OPENED="prey_opened";

	
	private String name;
	private JSONObject info;
	
	public Event(){
		
	}
	
	public Event(String name){
		this.name=name;
		this.info=new JSONObject();
	}
	
	
	public Event(String name,JSONObject info){
		this.name=name;
		this.info=info;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public JSONObject getInfo() {
		return info;
	}
	public void setInfo(JSONObject info) {
		this.info = info;
	}
	
}
