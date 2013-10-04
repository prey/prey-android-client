package com.prey.sms;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.actions.observer.ActionsController;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class SMSFactory {

	public static void execute(Context ctx,String command){
 
		 
			String secretKey=SMSUtil.getSecretKey(command);
			
			String email = PreyConfig.getPreyConfig(ctx).getEmail();
			boolean isPasswordOk =false;
			try {
				 isPasswordOk = PreyWebServices.getInstance().checkPassword(ctx, email,secretKey);
			} catch (PreyException e) {
				 
			}
			if (isPasswordOk){
				List<JSONObject> jsonList=SMSParser.getJSONListFromText(command);
				ActionsController.getInstance(ctx).runActionJson(ctx,jsonList);
			}
		}
	 
}
