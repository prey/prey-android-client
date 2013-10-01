package com.prey.sms;

import java.util.ArrayList;
import java.util.List;

public class SMSUtil {

	
	public static String getSecretKey(String command){
		List<String> commandList= getListCommand(command);
		return commandList.get(1);
	}
	public  static List<String> getCommand(String command){
		List<String> commandList= getListCommand(command);
		commandList.remove(0);
		commandList.remove(0);
		return commandList;
	}
	
	public  static boolean isValidSMSCommand(String command){
		List<String> commandList= getListCommand(command);
		if (commandList!=null&&commandList.size()>=3){
			if (!"prey".equals(commandList.get(0))){
				return false;
			}else{
				return true;
			}
		}else{
			return false;
		}
	}
	public static List<String> getListCommand(String command){
		String [] array=command.split(" ");
		List<String> list=new ArrayList<String>();
		for(int i=0;list!=null&&i<list.size();i++){
			list.add(array[i]);
		}
		return list;
	}
}
