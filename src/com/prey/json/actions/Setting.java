package com.prey.json.actions;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.util.StringUtil;

public class Setting {

	public static final String DATA_ID = "setting";
	
	public void update(Context ctx, List<ActionResult> lista, JSONObject parameters) {

		try{
			String key=parameters.getString("key");
			
		 
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
			SharedPreferences.Editor editor = settings.edit();
			
			String value=parameters.getString("value");
			if (StringUtil.isTextBoolean(value)){
				editor.putBoolean( key, Boolean.parseBoolean(value));
			}else{
				if(StringUtil.isTextInteger(value)){
					editor.putInt(key, Integer.parseInt(value));
				}else{
					editor.putString(key, value);
				}
			}
			editor.commit();
		} catch (JSONException e) {
			PreyLogger.e("Error, causa:"+e.getMessage(), e);
		}
	}
	
	public void read(Context ctx, List<ActionResult> lista, JSONObject parameters) {

		
		HttpDataService data = new HttpDataService(Setting.DATA_ID);
		
		try{
			String key=parameters.getString("key");
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
			HashMap<String, String> parametersMap = new HashMap<String, String>();
			 
			if (!"".equals(key)){
				String value=settings.getString(key, "");
				if (!"".equals(value)){
					parametersMap.put(key, value);
				}
			}
			data.getDataList().putAll(parametersMap);
			ActionResult result = new ActionResult();
			result.setDataToSend(data);
			
			lista.add(result);
			
			PreyLogger.d("Ejecuting Auto_update Action. DONE!");
			
		} catch (JSONException e) {
			PreyLogger.e("Error, causa:"+e.getMessage(), e);
		}
	}


}
