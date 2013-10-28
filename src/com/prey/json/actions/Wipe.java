package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
 
import com.prey.actions.observer.ActionResult;
import com.prey.actions.wipe.WipeThread;

public class Wipe {

	public void sms(Context ctx,List<ActionResult> lista,JSONObject parameters){
        boolean wipe=true;
        boolean deleteSD=false;
        String sd = null;
        try {
                sd=parameters.getString("parameter");
        }catch(Exception e){
                
        }
        if(sd!=null&&"sd".equals(sd)){
                wipe=false;
         deleteSD=true;
        }
        new WipeThread(ctx,wipe, deleteSD).start();
	}
	
}
