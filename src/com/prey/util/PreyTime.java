package com.prey.util;

import java.util.Calendar;

public class PreyTime {

 
	private PreyTime(){
		running=false;
	}
	
	private static PreyTime instance=null;
	
	private long time=0;
	
	private boolean running=false;
	
	public static PreyTime getInstance(){
		if(instance==null){
			instance=new PreyTime();
		}
		return instance;
	}
	
	public void setRunning(boolean running){
		this.running=running;
		if (running){
			Calendar cal=Calendar.getInstance();
			cal.add(Calendar.MINUTE, 1);
			time=cal.getTimeInMillis();
		}
	}
	public boolean isRunning(){
		Calendar cal=Calendar.getInstance();
		if (running && time<cal.getTimeInMillis()){
			return false;
		}
		return running;
	}
}
