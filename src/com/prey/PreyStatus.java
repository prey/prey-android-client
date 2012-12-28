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
	
	private boolean preyPopUpOnclick=false;
	
	private boolean isTakenPicture=false;
	
	private boolean isAlarmStart=false;

	
	public boolean isAlarmStart() {
		return isAlarmStart;
	}

	public void setAlarmStart() {
		this.isAlarmStart = true;
	}
	
	public void setAlarmStop() {
		this.isAlarmStart = false;
	}

	public boolean isTakenPicture() {
		return isTakenPicture;
	}

	public void setTakenPicture(boolean isTakenPicture) {
		this.isTakenPicture = isTakenPicture;
	}
	

	public boolean isPreyConfigurationActivityResume() {
		return preyConfigurationActivityResume;
	}

	public void setPreyConfigurationActivityResume(
			boolean preyConfigurationActivityResume) {
		this.preyConfigurationActivityResume = preyConfigurationActivityResume;
	}

	public boolean isPreyPopUpOnclick() {
		return preyPopUpOnclick;
	}

	public void setPreyPopUpOnclick(boolean preyPopUpOnclick) {
		this.preyPopUpOnclick = preyPopUpOnclick;
	}
	
	
}
