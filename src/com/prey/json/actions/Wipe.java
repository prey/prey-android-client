package com.prey.json.actions;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;

 
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
 

public class Wipe {

	public void start(Context ctx,List<ActionResult> lista,JSONObject parameters){
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","started"));
		
		
		
		try{
			deleteSD();
		}catch(Exception e){
		}
		try{
			if (preyConfig.isFroyoOrAbove()){
				PreyLogger.d("Wiping the device!!");
				FroyoSupport.getInstance(ctx).wipe();
			}
		}catch(Exception e){
			PreyLogger.e("Error Wipe1:"+e.getMessage(), e);
		}
		try{
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","stopped"));
		}catch(Exception e){
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","failed",e.getMessage()));
			PreyLogger.e("Error Wipe2:"+e.getMessage(), e);
		}
		
	}

	
	private void deleteSD(){
		String accessable = Environment.getExternalStorageState();
		PreyLogger.d("Deleting folder: " + accessable + " from SD");

	    if (Environment.MEDIA_MOUNTED.equals(accessable)) {
	    	File dir = new File(Environment.getExternalStorageDirectory()+"");
	    	deleteRecursive(dir);
	    }

	}

	private void deleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            deleteRecursive(child);
	    fileOrDirectory.delete();
	}
}
