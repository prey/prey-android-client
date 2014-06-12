package com.prey.actions.contact;

import org.json.JSONObject;

public class Email {
	private String address;
	private String type;
	private String label;
	private String contact;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	public JSONObject toJSONObject(){
		JSONObject json=new JSONObject();
		try{
			json.put("address",address);
			json.put("type",type);
			json.put("label",label);
			json.put("contact",contact);
		}catch(Exception e){}
		return json;
	}
	public String toString(){
		StringBuffer sb=new StringBuffer();
		String tab=" "; 
		sb.append("address:").append(address.toString()).append(tab);
		sb.append("type:").append(type.toString()).append(tab);
		sb.append("label:").append(label.toString()).append(tab);
		sb.append("contact:").append(contact.toString());
		return sb.toString();
	}
}
