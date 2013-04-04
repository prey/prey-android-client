package com.prey.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
 

public abstract class JsonAction {

	
	public List<HttpDataService> report(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.i(this.getClass().getName());
		List<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
		try {
			HttpDataService data = run(ctx, list, parameters);
			ActionResult result = new ActionResult();
			result.setDataToSend(data);
			list.add(result);
			
			dataToBeSent.add(data);
			
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
		return dataToBeSent;
	}
	
	public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.i(this.getClass().getName());
		HttpDataService data = run(ctx, list, parameters);
		ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
		dataToBeSent.add(data);
	//	PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
		return dataToBeSent;
	}

	public abstract HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters);
}
