package com.prey.json.actions;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.util.ClassUtil;

public class Report {

	public void get(Context ctx, List<ActionResult> lista, JSONObject parameters) {

		try {
			PreyConfig preyConfig=PreyConfig.getPreyConfig(ctx);
			preyConfig.setMissing(true);
			
			JSONArray jsonArray = parameters.getJSONArray("include");
			for (int i = 0; i < jsonArray.length(); i++) {
				String nameAction = jsonArray.getString(i);
				PreyLogger.i("nameAction:" + nameAction);
				String methodAction = "get";
				JSONObject parametersAction = null;
				ClassUtil.execute(ctx, lista, nameAction, methodAction, parametersAction);
			}

		/*	int interval = parameters.getInt("interval");
			JSONObject endPointObject = parameters.getJSONObject("endpoint");
			EndPoint endPoint = new EndPoint(endPointObject);
			PreyLogger.i("interval:" + interval + " endPoint:" + endPoint.getLocation());
*/
		} catch (JSONException e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		}
	}

}
