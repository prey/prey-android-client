package com.prey.json.actions;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.sms.SMSUtil;
 

public class Sms {

	public  void history(Context ctx, List<ActionResult> list, JSONObject parameters) {
		SMSUtil sms=new SMSUtil();
		JSONArray array=sms.history(ctx);
		if(array!=null)
			PreyLogger.i("array length:"+array.length());
		else
			PreyLogger.i("array empty");
	}
}
