package com.prey.actions.location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.PreyPhone.Wifi;
import com.prey.actions.HttpDataService;
import com.prey.exceptions.PreyException;
import com.prey.json.UtilJson;
import com.prey.managers.PreyWifiManager;
import com.prey.net.PreyWebServices;
import com.prey.services.LocationService;

public class LocationUtil {

	public static final String LAT = "lat";
	public static final String LNG = "lng";
	public static final String ACC = "accuracy";

	public static HttpDataService dataLocation(Context ctx) {
		HttpDataService data =null;
		try {
			LocationManager mlocManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
			boolean isGpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkEnabled = mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			PreyLogger.d("gps status:" + isGpsEnabled);
			PreyLogger.d("net status:" + isNetworkEnabled);
			PreyLocation location = null;
			if (isGpsEnabled || isNetworkEnabled) {
				int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
				if (ConnectionResult.SUCCESS == resultCode) {
					location = getPreyLocationPlayService(ctx);
				} else {
					location = getPreyLocationAppService(ctx);
				}
				if(location==null)
					location = getDataLocationWifi(ctx);
			} else {
				location = getDataLocationWifi(ctx);
			}
			PreyLogger.d("locationData:" + location.getLat()+" "+location.getLng()+" "+location.getAccuracy());
			data=convertData(location);
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage(), e);
			Map<String, String> parms = UtilJson.makeMapParam("get", "location", "failed", e.getMessage());
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, parms);
		}
		return data;
	}

	public static PreyLocation getDataLocationWifi(Context ctx) throws Exception {
		PreyLocation location = null;
		PreyPhone preyPhone = new PreyPhone(ctx);
		boolean disableWifi=false;
		if (!PreyWifiManager.getInstance(ctx).isWifiEnabled()) {
			disableWifi=true;
			PreyWifiManager.getInstance(ctx).setWifiEnabled(true);
			Thread.sleep(2000);
		}
		List<Wifi> listWifi = preyPhone.getListWifi();
		if (disableWifi){
			PreyWifiManager.getInstance(ctx).setWifiEnabled(false);
		}
		location = PreyWebServices.getInstance().getLocation(ctx, listWifi);
 
 
		
		
		return location;
	}

	public static PreyLocation getPreyLocationPlayService(Context ctx) throws Exception {
		PreyGooglePlayServiceLocation play = new PreyGooglePlayServiceLocation();
		play.init(ctx);
		try {
			play.startPeriodicUpdates();
		} catch (Exception e) {
		}
		int i=0;
		Location currentLocation = play.getLastLocation(ctx);
		while (currentLocation == null&&i<3) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			currentLocation = play.getLastLocation(ctx);
			i=i+1;
		}
		PreyLocation preyLocation=null;
		if(currentLocation!=null){
			preyLocation = new PreyLocation(currentLocation);
		}else{
			if(currentLocation==null){
				preyLocation = getPreyLocationAppService(ctx);
			}
			if(currentLocation==null){
				preyLocation = getDataLocationWifi(ctx);
			}
		}
		try {
			play.stopPeriodicUpdates();
		} catch (Exception e) {
		}
		return preyLocation;
	}

	public static PreyLocation getPreyLocationAppService(Context ctx) throws Exception {
		PreyLocation location = null;
		Intent intent = new Intent(ctx, LocationService.class);
		try {
			ctx.startService(intent);
			boolean validLocation = false;
			int i = 0;
			while (!validLocation) {
				location = PreyLocationManager.getInstance(ctx).getLastLocation();
				if (location.isValid()) {
					validLocation = true;
				} else {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						throw new PreyException("Thread was intrrupted. Finishing Location NotifierAction", e);
					}
					if (i > 2) {
						return null;
					}
					i++;
				}
			}
			ctx.stopService(intent);
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage(), e);
			Map<String, String> parms = UtilJson.makeMapParam("get", "location", "failed", e.getMessage());
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, parms);
		} finally {
			ctx.stopService(intent);
		}
		return location;
	}

	public static HttpDataService convertData(PreyLocation lastLocation) {
		if(lastLocation==null)
			return null;
		HttpDataService data = new HttpDataService("location");
		data.setList(true);
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		parametersMap.put(LAT, Double.toString(lastLocation.getLat()));
		parametersMap.put(LNG, Double.toString(lastLocation.getLng()));
		parametersMap.put(ACC, Float.toString(lastLocation.getAccuracy()));
		data.addDataListAll(parametersMap);
		return data;
	}

}
