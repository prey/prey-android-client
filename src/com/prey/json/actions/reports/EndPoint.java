package com.prey.json.actions.reports;

import org.json.JSONException;
import org.json.JSONObject;

public class EndPoint {

 private JSONObject json;
	 
	 public EndPoint(JSONObject json){
		 this.json=json;
	 }
	 
	 public String getType() throws JSONException{
		 return json.getString("type");
	 }
	 public String getLocation() throws JSONException{
		 return json.getString("location");
	 }
	 public String getMethod() throws JSONException{
		 JSONObject options= json.getJSONObject("options");
		 return options.getString("method");
	 }

}
