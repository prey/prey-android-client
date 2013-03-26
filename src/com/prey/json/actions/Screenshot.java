/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

 
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.net.http.EntityFile;
 
 

public class Screenshot extends JsonAction{

	public static final String DATA_ID = "screenshot";

	public void report(Context ctx, List<ActionResult> list, JSONObject parameters) {
		 super.report(ctx, list, parameters);
		 PreyLogger.d("Ejecuting Screenshot reports. DONE!");
	}
	
	public ArrayList<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting Screenshot Data.");
		ArrayList<HttpDataService> listResult=super.get(ctx, list, parameters);
		return listResult;
	}
	
	public  HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters){
		HttpDataService data =null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Bitmap bitmap = com.prey.actions.screenshot.ScreenShot.getScreenBitmap(ctx);
			bitmap.compress(CompressFormat.JPEG, 100, baos);
			InputStream file = new ByteArrayInputStream(((ByteArrayOutputStream) baos).toByteArray());
			EntityFile entityFile = new EntityFile();
			entityFile.setFile(file);
			entityFile.setMimeType("image/png");
			entityFile.setName("screenshot.jpg");
			entityFile.setType("screenshot");
			data = new HttpDataService(Screenshot.DATA_ID);
			data.setList(true);
			data.addEntityFile(entityFile);
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		} 
		return data;
	}

}
