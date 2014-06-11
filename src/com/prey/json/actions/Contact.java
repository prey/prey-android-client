package com.prey.json.actions;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.contact.ContactUtil;
import com.prey.actions.observer.ActionResult;

public class Contact {

	public  void history(Context ctx, List<ActionResult> list, JSONObject parameters) {
		ContactUtil contact=new ContactUtil();
		JSONArray array=contact.history(ctx);
		if(array!=null)
			PreyLogger.i("array length:"+array.length());
		else
			PreyLogger.i("array empty");
	}
}
