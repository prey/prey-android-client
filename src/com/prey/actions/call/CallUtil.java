package com.prey.actions.call;

import org.json.JSONArray;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.prey.PreyLogger; 

public class CallUtil {

	
	public JSONArray history(Context ctx){
		JSONArray array=new JSONArray();
		Cursor c = null;
		try{
			PreyCall objCall= new PreyCall();
			Uri allCalls = Uri.parse("content://call_log/calls");
	     	c = ctx.getContentResolver().query(allCalls, null, null, null, null);
	        int totalCall = c.getCount();
            if (c.moveToFirst()) {
            	for (int i = 0; i < totalCall; i++) {
            		objCall = new PreyCall();
            		objCall.setCachedName(c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME)));
            		objCall.setCachedNumberLabel(c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL)));
            		objCall.setCachedNumberType(c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE)));
            		objCall.setDate (c.getString(c.getColumnIndex(CallLog.Calls.DATE)));
            		objCall.setDuration(c.getString(c.getColumnIndex(CallLog.Calls.DURATION)));
            		objCall.setNewCall(c.getString(c.getColumnIndex(CallLog.Calls.NEW)));
            		objCall.setNumber (c.getString(c.getColumnIndex(CallLog.Calls.NUMBER)));
            		objCall.setType (c.getString(c.getColumnIndex(CallLog.Calls.TYPE)));
            		
            		 
            		 
            		PreyLogger.i(objCall.toString());
            		array.put(objCall.toJSONObject());
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
} 

 
