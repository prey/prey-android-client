package com.prey.actions.sms;

import org.json.JSONObject;

public class PreySms {

	private String id;
	private String address;
	private String msg;
	private String readState;
	private String time;
	private String folderName;
	private String subject;
	private String person;
	
 
 
	public String getPerson() {
		return person;
	}
	public void setPerson(String person) {
		this.person = person;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getReadState() {
		return readState;
	}
	public void setReadState(String readState) {
		this.readState = readState;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
 
          
	public String toString(){
		StringBuffer sb=new StringBuffer();
		String tab=" "; 
		sb.append("id:").append(id).append(tab);
		sb.append("address:").append(address).append(tab);
		sb.append("msg:").append(msg).append(tab);
		sb.append("readState:").append(readState).append(tab);
		sb.append("time:").append(time).append(tab);
		sb.append("folderName:").append(folderName);
		sb.append("subject:").append(subject);
		sb.append("person:").append(person);
		return sb.toString();
	}
	
 
	
	
	public JSONObject toJSONObject(){
		JSONObject json=new JSONObject();
		try{
		//json.put("id",id);
		json.put("address",id);
		json.put("body",msg);
		json.put("type",folderName);
		json.put("date",time);
		json.put("state",readState);
		json.put("subject",subject);
		json.put("person",person);
		}catch(Exception e){}
		return json;
	}
	
}
