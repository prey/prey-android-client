package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.camera.TakePictureCamera;
import com.prey.actions.observer.ActionResult;

public class Picture {

	public void get(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		PreyLogger.i(this.getClass().getName());
		try {

			TakePictureCamera takePictureCamera = new TakePictureCamera();
			HttpDataService data = takePictureCamera.takePicture(ctx);
			
			ActionResult result = new ActionResult();
			result.setDataToSend(data);
			
			lista.add(result);
			PreyLogger.d("Ejecuting CameraAction Action. DONE!");

		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
	}

}
