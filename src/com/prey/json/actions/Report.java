/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.util.ClassUtil;

public class Report  {

	public void get(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		try {
			List<HttpDataService> listData=new ArrayList<HttpDataService>();
			PreyConfig preyConfig=PreyConfig.getPreyConfig(ctx);
			preyConfig.setMissing(true);
			int interval = parameters.getInt("interval");
			while(preyConfig.isMissing()){
				JSONArray jsonArray = parameters.getJSONArray("include");
				for (int i = 0; i < jsonArray.length(); i++) {
					String nameAction = jsonArray.getString(i);
					PreyLogger.i("nameAction:" + nameAction);
					String methodAction = "report";
					JSONObject parametersAction = null;
					listData=ClassUtil.execute(ctx, lista, nameAction, methodAction, parametersAction,listData);
				}
				if (listData!=null&&listData.size()>0){
					PreyHttpResponse response=PreyWebServices.getInstance().sendPreyHttpReport(ctx, listData);
					if (200!=response.getStatusLine().getStatusCode()){
						preyConfig.setMissing(false);
					}else{
						Thread.sleep(interval * PreyConfig.DELAY_MULTIPLIER);
					}
				}else{
					preyConfig.setMissing(false);
				}
			}
		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("get","report","failed",e.getMessage()));
		}
	}
	
 

}
