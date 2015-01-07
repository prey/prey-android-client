package com.prey.events.factories;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
//import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.beta.actions.PreyBetaController;
import com.prey.events.Event;
import com.prey.managers.PreyConnectivityManager;
import com.prey.managers.PreyTelephonyManager;

public class EventFactory {

	private static final String BOOT_COMPLETED="android.intent.action.BOOT_COMPLETED";
	private static final String CONNECTIVITY_CHANGE="android.net.conn.CONNECTIVITY_CHANGE";
	private static final String WIFI_STATE_CHANGED="android.net.wifi.WIFI_STATE_CHANGED";
//	private static final String ACTION_POWER_CONNECTED="android.intent.action.ACTION_POWER_CONNECTED";
//	private static final String ACTION_POWER_DISCONNECTED="android.intent.action.ACTION_POWER_DISCONNECTED";
//	private static final String ACTION_BATTERY_CHANGE="android.intent.action.ACTION_BATTERY_CHANGE";
//	private static final String ACTION_BATTERY_LOW="android.intent.action.ACTION_BATTERY_LOW";
	private static final String ACTION_SHUTDOWN="android.intent.action.ACTION_SHUTDOWN";
//	private static final String BATTERY_LOW="android.intent.action.BATTERY_LOW";
//	private static final String BATTERY_OKAY="android.intent.action.BATTERY_OKAY";
//	private static final String BATTERY_CHANGED="android.intent.action.BATTERY_CHANGED";
	
	private static final String AIRPLANE_MODE="android.intent.action.AIRPLANE_MODE";
	
//	private static final String USER_PRESENT="android.intent.action.USER_PRESENT";
	
	public static Event getEvent(Context ctx,Intent intent){
		String message="getEvent["+intent.getAction()+"]";
		//Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
		PreyLogger.d(message);
		if (BOOT_COMPLETED.equals(intent.getAction()) ){
			if (PreyConfig.getPreyConfig(ctx).isSimChanged()){
				JSONObject info=new JSONObject();
				try{
					info.put("new_phone_number",PreyTelephonyManager.getInstance(ctx).getLine1Number());
				}catch (Exception e) {
				}
				return new Event(Event.SIM_CHANGED,info.toString());
			}else{
				return new Event(Event.TURNED_ON);
			}
		}
		if (ACTION_SHUTDOWN.equals(intent.getAction()) ){
			return new Event(Event.TURNED_OFF);
		}
		if (CONNECTIVITY_CHANGE.equals(intent.getAction())  ){
			JSONObject info=new JSONObject();
			
			int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
			PreyLogger.d("__wifiState:"+wifiState);;
			
			if (!PreyConnectivityManager.getInstance(ctx).isWifiConnected()) {
			  Bundle extras = intent.getExtras();
		        if (extras!=null){
		               if("connected".equals(extras.getString(ConnectivityManager.EXTRA_REASON))){
		            	   try{Thread.sleep(2000);}catch(Exception e){}
		            	   PreyConfig.getPreyConfig(ctx).registerC2dm();
		               }
		        }
			}
			try{
				if (!PreyConnectivityManager.getInstance(ctx).isMobileConnected()) {
					info.put("connected", "mobile");
					if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
						try{Thread.sleep(2000);}catch(Exception e){}
						PreyConfig.getPreyConfig(ctx).registerC2dm();
					}
				}
			}catch (Exception e) {
			}
			return new Event(Event.WIFI_CHANGED,info.toString());
		}
		if (  WIFI_STATE_CHANGED.equals(intent.getAction()) ){
			JSONObject info=new JSONObject();
			int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
			PreyLogger.d("___wifiState:"+wifiState);;
			try{
				if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
					info.put("connected", "wifi");
					try{Thread.sleep(2000);}catch(Exception e){}
					PreyConfig.getPreyConfig(ctx).registerC2dm();
				}
			}catch (Exception e) {
			}
			return new Event(Event.WIFI_CHANGED,info.toString());
		}
		if (AIRPLANE_MODE.equals(intent.getAction())){
			if(!isAirplaneModeOn(ctx)){
				try{Thread.sleep(4000);}catch(Exception e){}
				PreyBetaController.startPrey(ctx);
				PreyConfig.getPreyConfig(ctx).registerC2dm();
			}
		}
		/*
		if (ACTION_POWER_CONNECTED.equals(intent.getAction()) ){
			return new Event(Event.PLUGGED);
		}
		if (ACTION_POWER_DISCONNECTED.equals(intent.getAction()) ){
			return new Event(Event.UN_PLUGGED);
		}
			
		if (ACTION_BATTERY_LOW.equals(intent.getAction()) && isValidLowBattery(ctx)){
			return new Event(Event.BATTERY_LOW);
		}
		if (BATTERY_LOW.equals(intent.getAction()) && isValidLowBattery(ctx)){
			return new Event(Event.BATTERY_LOW);
		}
		if (BATTERY_OKAY.equals(intent.getAction()) ){
			return new Event(Event.BATTERY_OK);
		}
		if (BATTERY_CHANGED.equals(intent.getAction()) ){
			return new Event(Event.BATTERY_CHANGE);
		}
		if (ACTION_BATTERY_CHANGE.equals(intent.getAction())){
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			JSONObject info=new JSONObject();
			try{
				if(plugged==BatteryManager.BATTERY_PLUGGED_AC ){
					info.put("source ","ac");
				}else{
					if(plugged==BatteryManager.BATTERY_PLUGGED_USB){
						info.put("source ","usb");
					}
				}
			}catch (Exception e) {
			}
			//return new Event(Event.PLUGGED,info.toString());
		}*/
		return null;
	}
	
	public static boolean isAirplaneModeOn(Context context){
		   return Settings.System.getInt(context.getContentResolver
		                        (),Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	
	private static SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy hh:mm:ss",Locale.getDefault());
	
	public static boolean isValidLowBattery(Context ctx) {
		try{
			Calendar cal=Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR,-3);
			long leastThreeHours=cal.getTimeInMillis();
			long lowBatteryDate=PreyConfig.getPreyConfig(ctx).getLowBatteryDate();
			PreyLogger.d("lowBatteryDate :"+lowBatteryDate+" "+sdf.format(new Date(lowBatteryDate)));
			PreyLogger.d("leastMinutes   :"+leastThreeHours+" "+sdf.format(new Date(leastThreeHours)));
			if(lowBatteryDate==0||leastThreeHours>lowBatteryDate){
				long now=new Date().getTime();
				PreyConfig.getPreyConfig(ctx).setLowBatteryDate(now);
				return true;
			}
			return false;
		}catch(Exception e){
			return false;
		}
	}
}
