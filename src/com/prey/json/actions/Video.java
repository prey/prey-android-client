package com.prey.json.actions;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;


import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.video.VideoThread;
 



public class Video   {

	 public List<HttpDataService> sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
		 PreyLogger.d("Ejecuting sms Picture Data.");
         new VideoThread(ctx).start();
         List<HttpDataService> listResult=new ArrayList<HttpDataService>();
         return listResult;
        }

}