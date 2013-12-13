package com.prey.util;

import java.util.Calendar;

public class PreyTime {

 
	private PreyTime(){}
	
	private static PreyTime instance=null;
	
	private long time=0;
	
	public static PreyTime getInstance(){
		if(instance==null){
			instance=new PreyTime();
		}
		return instance;
	}

	public void setTimeC2dm(){
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.SECOND, 3);
		time=cal.getTimeInMillis();
	}
	public boolean isTimeC2dm(){
		Calendar cal=Calendar.getInstance();
		return (time<cal.getTimeInMillis());
	}
}
