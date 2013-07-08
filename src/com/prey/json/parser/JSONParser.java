package com.prey.json.parser;


import java.util.ArrayList;
import java.util.List;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.net.PreyRestHttpClient;

public class JSONParser {

	JSONObject jObj;

	boolean error;

	private final static String COMMAND = "\"command\"";

	// constructor
	public JSONParser() {

	}

	public List<JSONObject> getJSONFromUrl(Context ctx, String url) {
		PreyLogger.d("getJSONFromUrl:" + url);
		PreyRestHttpClient preyRestHttpClient=PreyRestHttpClient.getInstance(ctx);
		String sb=null;
		String json=null;
		try{
			;
			sb=preyRestHttpClient.getStringUrl(url,PreyConfig.getPreyConfig(ctx));
			if (sb!=null)
				json = sb.trim();
		}catch(Exception e){
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
			return null;
		}
	 
	//	json = "[{\"command\":\"get\",\"target\":\"picture\",\"options\":{}}]";
		
		//json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\"]}}]";

		// json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";

		// json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\",\"picture\",\"location\"]}}]";

		// json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\"]}}]";

		// json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"location\"]}}]";

		// json="[ {\"command\": \"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.60713481,-36.42372147\",\"radius\": \"100\" }}]";

		// json="[ {\"command\": \"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.7193117,-32.7521112\",\"radius\": \"100\" }}]";

		// json="[ {\"command\": \"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.60713481,-33.42372147\",\"radius\": \"100\" }}]";
		// json="[ {\"command\": \"stop\",\"target\": \"geofencing\",\"options\": {}}]";

		// json="[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}},{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}, {\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\"[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";

		// json="[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}, {\"command\":\"get\",\"target\":\"report\",\"options\":{\"delay\": \"25\",\"include\"[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";

		// json="[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}
		
		//json = "[ {\"command\": \"data\",\"target\": \"location\",\"options\": {}}]";
		
		// json="[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}]"; 
	//	 json="[{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}]"; 

		
	//	 json="[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"screenshot\",\"access_points_list\"],\"interval\":\"10\"}}]"; 
				
		
		 //json="[{\"command\":\"start\",\"target\":\"camouflage\",\"options\":null}]"; 
		// json="[{\"command\":\"stop\",\"target\":\"camouflage\",\"options\":null}]"; 
		 
		if ("[]".equals(json)) {
			return null;
		}
		return getJSONFromTxt(ctx, json);
	}

	public List<JSONObject> getJSONFromTxt(Context ctx, String json) {
		jObj = null;
		List<JSONObject> listaJson = new ArrayList<JSONObject>();
		List<String> listCommands = getListCommands(json);
		for (int i = 0; listCommands != null && i < listCommands.size(); i++) {
			String command = listCommands.get(i);
			try {
				jObj = new JSONObject(command);
				listaJson.add(jObj);
			} catch (JSONException e) {
				PreyLogger.e("JSON Parser, Error parsing data " + e.toString(), e);
			}
		}
		PreyLogger.i("json:" + json);
		// return JSON String
		return listaJson;
	}

	private List<String> getListCommands(String json) {
		json = json.replaceAll("nil", "{}");
		json = json.replaceAll("null", "{}");
		List<String> lista = new ArrayList<String>();
		int posicion = json.indexOf(COMMAND);
		json = json.substring(posicion + 9);
		posicion = json.indexOf(COMMAND);
		String command = "";
		while (posicion > 0) {
			command = json.substring(0, posicion);
			json = json.substring(posicion + 9);
			lista.add("{" + COMMAND + cleanChar(command));
			posicion = json.indexOf("\"command\"");
		}
		lista.add("{" + COMMAND + cleanChar(json));
		return lista;
	}

	private String cleanChar(String json) {
		if (json != null) {
			json = json.trim();
			char c = json.charAt(json.length() - 1);
			while (c == '{' || c == ',' || c == ']') {
				json = json.substring(0, json.length() - 1);
				json = json.trim();
				c = json.charAt(json.length() - 1);
			}
		}
		return json;
	}
}
