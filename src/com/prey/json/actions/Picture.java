/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.camera.TakePictureCamera;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;


public class Picture extends JsonAction {

	public void report(Context ctx, List<ActionResult> list, JSONObject parameters) {
		 super.report(ctx, list, parameters);
		 PreyLogger.d("Ejecuting Picture reports. DONE!");
	}
	
	public ArrayList<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting Picture Data.");
		ArrayList<HttpDataService> listResult=super.get(ctx, list, parameters);
		return listResult;
	}
	
	
	public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters){
		HttpDataService data =null;
		try {
			TakePictureCamera takePictureCamera = new TakePictureCamera();
			data = takePictureCamera.takePicture(ctx);
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
		return data;
	}

}
