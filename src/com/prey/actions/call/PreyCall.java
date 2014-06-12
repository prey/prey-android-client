package com.prey.actions.call;

import org.json.JSONObject;

public class PreyCall {

	
	private String cachedName;
	private String cachedNumberLabel;
	private String cachedNumberType;
	private String date;
	private String duration;
	private String newCall;
	private String number;
	private String type;
	
	
	
	public String toString(){
		StringBuffer sb=new StringBuffer();
		String tab=" "; 
		sb.append("cachedName:").append(cachedName).append(tab);
		sb.append("cachedNumberLabel:").append(cachedNumberLabel).append(tab);
		sb.append("cachedNumberType:").append(cachedNumberType).append(tab);
		sb.append("date:").append(date).append(tab);
		sb.append("duration:").append(duration).append(tab);
		sb.append("newCall:").append(newCall);
		sb.append("number:").append(number);
		sb.append("type:").append(type);
		return sb.toString();
	}
	
 
	
	
	public JSONObject toJSONObject(){
		JSONObject json=new JSONObject();
		try{
		//json.put("id",id);
		json.put("cachedName",cachedName);
		json.put("cachedNumberLabel",cachedNumberLabel);
		json.put("cachedNumberType",cachedNumberType);
		json.put("date",date);
		json.put("duration",duration);
		json.put("newCall",newCall);
		json.put("number",number);
		json.put("type",type);
		}catch(Exception e){}
		return json;
	}
	
	public String getCachedName() {
		return cachedName;
	}
	public void setCachedName(String cachedName) {
		this.cachedName = cachedName;
	}
	public String getCachedNumberLabel() {
		return cachedNumberLabel;
	}
	public void setCachedNumberLabel(String cachedNumberLabel) {
		this.cachedNumberLabel = cachedNumberLabel;
	}
	public String getCachedNumberType() {
		return cachedNumberType;
	}
	public void setCachedNumberType(String cachedNumberType) {
		this.cachedNumberType = cachedNumberType;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String isNewCall() {
		return newCall;
	}
	public void setNewCall(String newCall) {
		this.newCall = newCall;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	
}
