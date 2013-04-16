package com.prey.json.actions;

import java.util.ArrayList;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context; 

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;


public class ControlPanelHost {

 
	
	public List<HttpDataService> read(Context ctx, List<ActionResult> lista, JSONObject parameters) {

		ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
		
		

		try {
			String preyDomain = PreyConfig.getPreyConfig(ctx).getPreyDomain();
			
			
			HttpDataService data = new HttpDataService("control_panel_host");
			 
			 
			data.setSingleData(preyDomain);
			
			
			
 
			
			 
			
	 
			ActionResult result = new ActionResult();
			result.setDataToSend(data);
			lista.add(result);
			
			
			 
			dataToBeSent.add(data);
		//	PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
			
			
			
			PreyLogger.d("Ejecuting ControlPanelHost Action. DONE!");
			
			
		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		}

		
		return dataToBeSent;
		
		 
	}

	public void update(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		try {
			String url = parameters.getString("url");
			PreyConfig.getPreyConfig(ctx).setPreyDomain(url);
			//TODO:Falta evento
		} catch (JSONException e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		}
	}
}
