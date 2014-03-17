package com.prey.beta.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionsController;
import com.prey.exceptions.PreyException;
import com.prey.managers.PreyConnectivityManager;
import com.prey.managers.PreyTelephonyManager;
import com.prey.managers.PreyWifiManager;
import com.prey.net.NetworkUtils;
import com.prey.net.PreyWebServices;
import com.prey.services.LocationService;
import com.prey.beta.services.PreyBetaRunnerService;

public class PreyBetaActionsRunner implements Runnable {

	private Context ctx;
	private PreyConfig preyConfig = null;

	public PreyBetaActionsRunner(Context context) {
		this.ctx = context;
		
	}

	public void run() {
		execute();
	}
	public List<HttpDataService> execute() {
		List<HttpDataService> listData=null;
		
		preyConfig = PreyConfig.getPreyConfig(ctx);
	 	if (preyConfig.isThisDeviceAlreadyRegisteredWithPrey(true)){
	 		PreyTelephonyManager preyTelephony = PreyTelephonyManager.getInstance(ctx);
			PreyConnectivityManager preyConnectivity = PreyConnectivityManager.getInstance(ctx);
	 		boolean connection=false;
	 		try {
	 			while(!connection){
	 				connection= preyTelephony.isDataConnectivityEnabled() || preyConnectivity.isConnected();
	 				if(!connection){
						PreyLogger.d("Phone doesn't have internet connection now. Waiting 10 secs for it");
						
						if(!PreyWifiManager.getInstance(ctx).isWifiEnabled())
							PreyWifiManager.getInstance(ctx).setWifiEnabled(true);
						if(!NetworkUtils.getNetworkUtils(ctx).isMobileDataEnabled())
							NetworkUtils.getNetworkUtils(ctx).enableMobileData(true);
						
						Thread.sleep(10000);
					}
	 			}
	 			listData=getInstructionsJsonAndRun(ctx);
			} catch (Exception e) {
				PreyLogger.e("Error, because:"+e.getMessage(),e );
			}
			ctx.stopService(new Intent(ctx, LocationService.class));
			ctx.stopService(new Intent(ctx, PreyBetaRunnerService.class));
			PreyLogger.d("Prey execution has finished!!");
	 	}
	 	return listData;
	}
	
	private List<HttpDataService> getInstructionsJsonAndRun(Context ctx) throws PreyException {
		List<HttpDataService> listData=null;
		List<JSONObject> jsonObject = null;
		try {
			jsonObject = PreyWebServices.getInstance().getActionsJsonToPerform(ctx);			
			listData = ActionsController.getInstance(ctx).runActionJson(ctx,jsonObject);			 
		} catch (PreyException e) {
			PreyLogger.e("Exception getting device's xml instruction set", e);
			throw e;
		}
		return listData;
	}
 
	 

}
 