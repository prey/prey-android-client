package com.prey.json.actions;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.call.CallUtil;
import com.prey.actions.observer.ActionResult;


public class Call {

	public  void history(Context ctx, List<ActionResult> list, JSONObject parameters) {
		CallUtil call=new CallUtil();
		JSONArray array=call.history(ctx);
		if(array!=null)
			PreyLogger.i("array length:"+array.length());
		else
			PreyLogger.i("array empty");
	}
}
