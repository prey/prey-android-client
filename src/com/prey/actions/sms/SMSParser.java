package com.prey.actions.sms;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

 

@SuppressLint("DefaultLocale")
public class SMSParser {

	public static List<JSONObject> getJSONListFromText(String command) {
		List<JSONObject> jsonObjectList =null;
		List<String> listCommand = SMSUtil.getListCommand(command);
		try {
			if (listCommand != null) {
				JSONObject json = new JSONObject();

				json.put("command", "sms");

				json.put("target", listCommand.get(2));
				if (listCommand.size() == 3) {
					json.put("options", null);
				} else{
					JSONObject jsonParameter = new JSONObject();
					 
					String parameter="";
					for(int i=3;listCommand!=null&&i<listCommand.size();i++){
						parameter=parameter+" "+listCommand.get(i).toLowerCase();
					}
					parameter=parameter.trim();
					jsonParameter.put("parameter", parameter);
					json.put("options", jsonParameter);
				}
				jsonObjectList = new ArrayList<JSONObject>();
				jsonObjectList.add(json);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObjectList;
	}

}
