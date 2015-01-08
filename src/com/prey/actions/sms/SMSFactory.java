package com.prey.actions.sms;

 

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionsController;
import com.prey.managers.PreyConnectivityManager;
import com.prey.managers.PreyTelephonyManager;
import com.prey.managers.PreyWifiManager;
import com.prey.net.NetworkUtils;
import com.prey.net.PreyWebServices;

public class SMSFactory {

    public static void execute(Context ctx,String command,String phoneNumber){
        String secretKey=SMSUtil.getSecretKey(command);
        String email = PreyConfig.getPreyConfig(ctx).getEmail();
        boolean isPasswordOk =false;
		PreyTelephonyManager preyTelephony = PreyTelephonyManager.getInstance(ctx);
		PreyConnectivityManager preyConnectivity = PreyConnectivityManager.getInstance(ctx);
		boolean connection=false;
		boolean openWifi=false;
		boolean openNet=false;
		int i=0;
 		try {
 			while(!connection&&i<5){
 				connection= preyTelephony.isDataConnectivityEnabled() || preyConnectivity.isConnected();
 				if(!connection){
					PreyLogger.d("Phone doesn't have internet connection now. Waiting 10 secs for it");
					if(!PreyWifiManager.getInstance(ctx).isWifiEnabled()){
						PreyWifiManager.getInstance(ctx).setWifiEnabled(true);
						openWifi=true;
					}
					if(!NetworkUtils.getNetworkUtils(ctx).isMobileDataEnabled()){
						NetworkUtils.getNetworkUtils(ctx).enableMobileData(true);
						openNet=true;
 					}
					Thread.sleep(10000);
				}
 			}
		} catch (Exception e) {
			PreyLogger.e("Error, because:"+e.getMessage(),e );
		}
        try {
             isPasswordOk = PreyWebServices.getInstance().checkPassword(ctx, email,secretKey);
        } catch (Exception e) {
        	PreyLogger.e("Error, because:"+e.getMessage(),e );
        }
        try {
        	if (isPasswordOk){
                List<JSONObject> jsonList=SMSParser.getJSONListFromText(command,phoneNumber);
                ActionsController.getInstance(ctx).runActionJson(ctx,jsonList);
        	}
        } catch (Exception e) {
        	PreyLogger.e("Error, because:"+e.getMessage(),e );
        }
        if(openWifi){
			PreyWifiManager.getInstance(ctx).setWifiEnabled(false);
			openWifi=true;
		}
		if(openNet){
			NetworkUtils.getNetworkUtils(ctx).enableMobileData(false);
		}
    }

}
