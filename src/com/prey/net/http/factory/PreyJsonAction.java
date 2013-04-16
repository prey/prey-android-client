package com.prey.net.http.factory;

public class PreyJsonAction {
	
	private static PreyJsonAction instance=null;
	
	private String action="[]";
	private PreyJsonAction(){
		
	}
	
	public static PreyJsonAction getInstance(){
		if(instance==null){
			instance=new PreyJsonAction();
		}
		return instance;
	}
	
	public String getAction(){
		return action;
	}

	public void setAction(String action){
		this.action= action;
	}
}
