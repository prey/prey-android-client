package com.prey.actions.camouflage;

 
public class VolumeCounter {

	
	private VolumeCounter(){
		counter=0;
	}
	
	private static VolumeCounter instance;
	
	private int counter=0;
	private long time=0;
	

	public static VolumeCounter getInstance(){
		if(instance==null)
			instance=new VolumeCounter();
		return instance;
	}
	
	public boolean update(int volumen,int prev,long timeUpdate){
		if (volumen!=prev){
			counter=0;
		}else{
			if (counter==0){
				time=timeUpdate;
				counter=counter+1;
			}else{
				if (time==timeUpdate){
					//nothing
				}else{
					if ((time+1)==timeUpdate){
						time=timeUpdate;
						counter=counter+1;
					}else{
						counter=0;
					}
				}
			}
		}
		if(counter>=7){
			counter=0;
			return true;
		}else{
			return false;
		}
	}
}
