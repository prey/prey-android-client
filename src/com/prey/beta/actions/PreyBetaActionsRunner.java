package com.prey.beta.actions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionsController;
import com.prey.exceptions.PreyException;
import com.prey.managers.PreyConnectivityManager;
import com.prey.managers.PreyTelephonyManager;
import com.prey.managers.PreyWifiManager;
import com.prey.net.NetworkUtils;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

public class PreyBetaActionsRunner implements Runnable {

	private Context ctx;
	private String body;
	private String version;
	private PreyConfig preyConfig = null;

	public PreyBetaActionsRunner(Context context) {
		this.ctx = context;
		this.body = "";
		this.version = "";
	}
	
	public PreyBetaActionsRunner(Context context,String body,String version) {
		this.ctx = context;
		this.body = body;
		this.version = version;
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
	 			List<JSONObject> jsonObject = getInstructions();
	 			PreyLogger.d("version:"+version+" body:"+body);
	 			if(jsonObject==null||jsonObject.size()==0){
	 				PreyLogger.d("nothing");
	 			}else{
	 				PreyLogger.d("runInstructions");
	 				runInstructions(jsonObject);
	 			}
			} catch (Exception e) {
				PreyLogger.e("Error, because:"+e.getMessage(),e );
			}
			PreyLogger.d("Prey execution has finished!!");
	 	}
	 	return listData;
	}

	private List<JSONObject> getInstructions() throws PreyException {
		PreyLogger.d("______________________________");
		PreyLogger.d("_______getInstructions________");
		List<JSONObject> jsonObject = null;
		try {
			jsonObject = PreyWebServices.getInstance().getActionsJsonToPerform(ctx);			
		} catch (PreyException e) {
			PreyLogger.e("Exception getting device's xml instruction set", e);
			throw e;
		}
		return jsonObject;
	}
	
	private List<HttpDataService> runInstructions(List<JSONObject> jsonObject) throws PreyException {
		List<HttpDataService> listData=null;
		listData = ActionsController.getInstance(ctx).runActionJson(ctx,jsonObject);			 
		return listData;
	}
	
	 
	
	
}
 