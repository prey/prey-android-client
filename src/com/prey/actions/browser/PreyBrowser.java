package com.prey.actions.browser;

import org.json.JSONObject;

public class PreyBrowser {

	private String bookmark;
	private String created;
	private String date;
	private String favicon;
	private String title;
	private String url;
	private String visits;
	
	public String toString(){
		StringBuffer sb=new StringBuffer();
		String tab=" "; 
		sb.append("bookmark:").append(	bookmark).append(tab);
		sb.append("created:").append(created).append(tab);
		sb.append("date:").append(date).append(tab);
		sb.append("favicon:").append(favicon).append(tab);
		sb.append("title:").append(title).append(tab);
		sb.append("url:").append(url);
		sb.append("visits:").append(visits); 
		return sb.toString();
	}
	
 
	
	
	public JSONObject toJSONObject(){
		JSONObject json=new JSONObject();
		try{
		//json.put("id",id);
		json.put("bookmark",	bookmark);
		json.put("created",created);
		json.put("date",date);
		json.put("favicon",favicon);
		json.put("title",title);
		json.put("url",url);
		json.put("visits",visits); 
		}catch(Exception e){}
		return json;
	}
	public String getBookmark() {
		return bookmark;
	}
	public void setBookmark(String bookmark) {
		this.bookmark = bookmark;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getFavicon() {
		return favicon;
	}
	public void setFavicon(String favicon) {
		this.favicon = favicon;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getVisits() {
		return visits;
	}
	public void setVisits(String visits) {
		this.visits = visits;
	}
	
 
	
}
