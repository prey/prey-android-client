package com.prey;

public class PreyStatus {

	
	private PreyStatus (){
		
	}
	
	private static PreyStatus instance=null;
	
	public static PreyStatus getInstance(){
		if (instance==null){
			instance=new PreyStatus();
		}
		return instance;
	}
	
	private boolean preyConfigurationActivityResume=false;

	public boolean isPreyConfigurationActivityResume() {
		return preyConfigurationActivityResume;
	}

	public void setPreyConfigurationActivityResume(
			boolean preyConfigurationActivityResume) {
		this.preyConfigurationActivityResume = preyConfigurationActivityResume;
	}
	
	
}
