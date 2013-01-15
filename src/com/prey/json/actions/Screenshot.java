package com.prey.json.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
 
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult; 
import com.prey.net.http.EntityFile;
 

public class Screenshot {

	public static final String DATA_ID = "screenshot";

	public void get(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		PreyLogger.i(this.getClass().getName());
 
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
			HttpDataService data = new HttpDataService(Screenshot.DATA_ID);
			data.setList(true);
			data.addEntityFile(entityFile);

			ActionResult result = new ActionResult();
			result.setDataToSend(data);

			lista.add(result);
			
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
		 
	}

}
