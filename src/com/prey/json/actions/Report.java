/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.observer.ActionsController;
import com.prey.json.UtilJson;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;
import com.prey.util.ClassUtil;

public class Report  {

	public void get(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		try {
			boolean valida=valida(ctx);
			if (!valida){
				return;
			}
			long lastReportStartDate=new Date().getTime();
			PreyLogger.d("____lastReportStartDate:"+lastReportStartDate);
			PreyConfig.getPreyConfig(ctx).setLastReportStartDate(lastReportStartDate);
			List<HttpDataService> listData=new ArrayList<HttpDataService>();
			 
			PreyConfig.getPreyConfig(ctx).setMissing(true);
			int interval =0;
			try{
				interval = parameters.getInt("interval");
			}catch(Exception e){
				interval =0;
			}
			PreyLogger.i("interval:"+interval);
			PreyConfig.getPreyConfig(ctx).setIntervalReport(""+interval);
			while(PreyConfig.getPreyConfig(ctx).isMissing()){
				
				JSONArray jsonArray = null;
				try{
					jsonArray=parameters.getJSONArray("include");
				}catch(Exception e){
					jsonArray=new JSONArray();
					jsonArray.put(new String("picture"));
					jsonArray.put(new String("location"));
					jsonArray.put(new String("access_points_list"));
				}
				for (int i = 0; i < jsonArray.length(); i++) {
					String nameAction = jsonArray.getString(i);
					PreyLogger.d("nameAction:" + nameAction);
					String methodAction = "report";
					JSONObject parametersAction = null;
					listData=ClassUtil.execute(ctx, lista, nameAction, methodAction, parametersAction,listData);
				}
				
				int parms=0;
				for (int i=0;listData!=null&&i<listData.size();i++) {
					HttpDataService httpDataService=listData.get(i);
					parms=parms+httpDataService.getDataAsParameters().size();
					if (httpDataService.getEntityFiles()!=null){
						for(int j=0;j<httpDataService.getEntityFiles().size();j++){
							EntityFile entity= httpDataService.getEntityFiles().get(j);
							if (entity!=null&&entity.getLength()>0){
								parms=parms+1;
							}
						}
					}
				}
				
				if (parms>0){
					PreyHttpResponse response=PreyWebServices.getInstance().sendPreyHttpReport(ctx, listData);
					if(response!=null){
						PreyLogger.d("response.getStatusLine():"+response.getStatusLine());	
						if (200!=response.getStatusLine().getStatusCode()){
							PreyConfig.getPreyConfig(ctx).setMissing(false);
						}else{
							PreyConfig.getPreyConfig(ctx).setLastEvent("report_send");
							if (interval==0){
								PreyConfig.getPreyConfig(ctx).setMissing(false);
							}else{
								Thread.sleep(interval * PreyConfig.DELAY_MULTIPLIER);
							}
						}
					}else{
						Thread.sleep(interval * PreyConfig.DELAY_MULTIPLIER);
					}
				}else{
					PreyConfig.getPreyConfig(ctx).setMissing(false);
					PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("get","report","failed","Unable to retrieve data"));
				}
				PreyLogger.d("fin missing:"+PreyConfig.getPreyConfig(ctx).isMissing());
			}
			PreyConfig.getPreyConfig(ctx).setIntervalReport("");
			PreyConfig.getPreyConfig(ctx).setLastReportStartDate(0);
			PreyLogger.i("Report completed");
		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("get","report","failed",e.getMessage()));
		}
	}
	
	public boolean valida(Context ctx){
		long lastReportStartDate=PreyConfig.getPreyConfig(ctx).getLastReportStartDate();
		PreyLogger.d("last:"+lastReportStartDate);
		if(lastReportStartDate!=0){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(lastReportStartDate);
			cal.add(Calendar.MINUTE, 2);
			long timeMore=cal.getTimeInMillis();
			PreyLogger.d("timM:"+timeMore);
			Date nowDate=new Date();
			long now = nowDate.getTime();
			PreyLogger.d("now_:"+now);
			PreyLogger.d("now>=timeMore:"+(now>=timeMore));
			return (now>=timeMore);
		}
		return true;
	}
	
	public static void run(Context ctx,int intervalReport){
		final int interval=intervalReport;
		final Context myContext=ctx;
		new Thread(){
		            public void run() {
		            	try{
		            	Thread.sleep(90000);
						List<JSONObject> list=new ArrayList<JSONObject>();
						JSONObject jsonParams=new JSONObject();
						jsonParams.put("interval",interval );
						JSONObject json=new JSONObject();
						json.put("command", "get");
						json.put("target", "report");
						json.put("options",	jsonParams);
						list.add(json);
		            	ActionsController.getInstance(myContext).runActionJson(myContext,list);
		            	}catch(Exception e){
		    				
		    			}
		            }
		}.start();
	}

}
