package com.prey.actions.contact;

import org.json.JSONObject;

public class NickName {
	public String name;
	public String label;
	public String type;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
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
			json.put("name",name);
			json.put("label",label);
			json.put("type",type);
		}catch(Exception e){}
		return json;
	}
	public String toString(){
		StringBuffer sb=new StringBuffer();
		String tab=" "; 
		sb.append("name:").append(name.toString()).append(tab);
		sb.append("label:").append(label.toString()).append(tab);
		sb.append("type:").append(type.toString());
		return sb.toString();
	}
}
