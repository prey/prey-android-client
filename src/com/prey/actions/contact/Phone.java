package com.prey.actions.contact;

import org.json.JSONObject;

public class Phone {
	private String number;
	private String type;
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
	public JSONObject toJSONObject(){
		JSONObject json=new JSONObject();
		try{
			json.put("number",number);
			json.put("type",type);
		}catch(Exception e){}
		return json;
	}
	public String toString(){
		StringBuffer sb=new StringBuffer();
		String tab=" "; 
		sb.append("number:").append(number.toString()).append(tab);
		sb.append("type:").append(type.toString());
		return sb.toString();
	}
}
