package com.prey.json.actions;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.browser.BrowserUtil;
import com.prey.actions.observer.ActionResult;

public class Browser {

	public  void history(Context ctx, List<ActionResult> list, JSONObject parameters) {
		BrowserUtil browser=new BrowserUtil();
		JSONArray array=browser.history(ctx);
		if(array!=null)
			PreyLogger.i("array length:"+array.length());
		else
			PreyLogger.i("array empty");
	}
}
