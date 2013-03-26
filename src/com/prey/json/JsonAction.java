package com.prey.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.net.PreyWebServices;

public abstract class JsonAction {

	
	public void report(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.i(this.getClass().getName());
		try {
			HttpDataService data = run(ctx, list, parameters);
			ActionResult result = new ActionResult();
			result.setDataToSend(data);
			list.add(result);
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
	}
	
	public ArrayList<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.i(this.getClass().getName());
		HttpDataService data = run(ctx, list, parameters);
		ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
		dataToBeSent.add(data);
		PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
		return dataToBeSent;
	}

	public abstract HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters);
}
