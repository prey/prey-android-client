package com.prey.actions.sms;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import com.prey.PreyLogger;
 

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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
	
	@SuppressLint("DefaultLocale")
	public  static boolean isValidSMSCommand(String command){
		try{
			List<String> commandList= getListCommand(command);
			if (commandList!=null&&commandList.size()>=3){
				if (!"prey".equals(commandList.get(0).toLowerCase())){
					return false;
				}else{
					return true;
				}
			}else{
				return false;
			}
		}catch(Exception e){
			return false;
		}
	}
	public static List<String> getListCommand(String command){
		String [] array=command.split(" ");
		List<String> list=new ArrayList<String>();
		for(int i=0;array!=null&&i<array.length;i++){
			list.add(array[i]);
		}
		return list;
	}
	
	public JSONArray history(Context ctx){
		JSONArray array=new JSONArray();
		Cursor c = null;
		try{
			PreySms objSms = new PreySms();
			Uri message = Uri.parse("content://sms/");
	     	c = ctx.getContentResolver().query(message, null, null, null, null);
	        int totalSMS = c.getCount();
            if (c.moveToFirst()) {
            	for (int i = 0; i < totalSMS; i++) {
            		objSms = new PreySms();
            		objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
            		objSms.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
            		objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
            		objSms.setReadState(c.getString(c.getColumnIndex("read")));
            		objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
            		objSms.setSubject(c.getString(c.getColumnIndexOrThrow("subject")));
            		objSms.setPerson(c.getString(c.getColumnIndexOrThrow("person")));
            		String typeFolder=typeFolder(c.getString(c.getColumnIndexOrThrow("type")));
            		objSms.setFolderName(typeFolder);
            		PreyLogger.i(objSms.toString());
            		array.put(objSms.toJSONObject());
	                c.moveToNext();
	            }
	        }
		}catch(Exception e){
			 
		}finally{
			if (c!=null){
				 c.close();
			}
		}
 		return array;
	}

	public String typeFolder(String type){
		String typeFolder="";
		if (type.contains("1")) {
			typeFolder="inbox";
        } else {
        	if (type.contains("2")) {
        		typeFolder="failed";
            } else {
            	if (type.contains("3")) {
            		typeFolder="queued";
	            } else {
	            	if (type.contains("4")) {
	            		typeFolder="sent";
		            } else {
		            	if (type.contains("5")) {
		            		typeFolder="draft";
			            } else {
			            	typeFolder="outbox";
			            }
		            }
	            }
            }
        }
		return typeFolder;
	}
}
