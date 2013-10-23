package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
 
import com.prey.actions.alarm.AlarmThread;
import com.prey.actions.observer.ActionResult;
 

public class Alarm {

	public void sms(Context ctx,List<ActionResult> lista,JSONObject parameters){
        new AlarmThread(ctx).start();
	}
	
}
