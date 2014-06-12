package com.prey.actions.contact;

import org.json.JSONObject;

public class Photo {
	private String displayName;
	private String photo;
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public JSONObject toJSONObject(){
		JSONObject json=new JSONObject();
		try{
			json.put("displayName",displayName);
			json.put("photo",photo);
		}catch(Exception e){}
		return json;
	}
	
	public String toString(){
		StringBuffer sb=new StringBuffer();
		String tab=" "; 
		sb.append("displayName:").append(displayName.toString()).append(tab);
		sb.append("photo:").append(photo.toString());
		return sb.toString();
	}
}
