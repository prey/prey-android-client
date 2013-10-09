package com.prey.actions.contact;

import org.json.JSONObject;

public class PreyContact {

	private NickName nickname;
	private Email email;	
	private Phone phone;
	private Photo photo;
	public NickName getNickname() {
		return nickname;
	}
	public void setNickname(NickName nickname) {
		this.nickname = nickname;
	}
	public Email getEmail() {
		return email;
	}
	public void setEmail(Email email) {
		this.email = email;
	}
	public Phone getPhone() {
		return phone;
	}
	public void setPhone(Phone phone) {
		this.phone = phone;
	}
	public Photo getPhoto() {
		return photo;
	}
	public void setPhoto(Photo photo) {
		this.photo = photo;
	}
	
	
	public JSONObject toJSONObject(){
		JSONObject json=new JSONObject();
		try{
			json.put("nickname",nickname.toJSONObject());
			json.put("email",email.toJSONObject());
			json.put("phone",phone.toJSONObject());
			json.put("photo",photo.toJSONObject());
		}catch(Exception e){}
		return json;
	}
	 
	public String toString(){
		StringBuffer sb=new StringBuffer();
		String tab=" "; 
		sb.append("nickname:").append(nickname.toString()).append(tab);
		sb.append("email:").append(email.toString()).append(tab);
		sb.append("phone:").append(phone.toString()).append(tab);
		sb.append("photo:").append(photo.toString());
		return sb.toString();
	}
	
}
