/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.camera;

 
import android.content.Context;


 

import com.prey.PreyLogger;


import com.prey.actions.HttpDataService;
import com.prey.actions.PreyAction;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;
import com.prey.exceptions.PreyException;

public class CameraAction extends PreyAction {

	public static final String DATA_ID = "webcam";
	 

	public CameraAction() {
		PreyLogger.d("Ejecuting CameraAction Action");
		
	}

	@Override
	public String textToNotifyUserOnEachReport(Context ctx) {
		return "";
	}

	@Override
	public void execute(ActionJob actionJob, Context ctx) throws PreyException {
		TakePictureCamera takePictureCamera=new TakePictureCamera();
		
		HttpDataService data = takePictureCamera.takePicture(ctx);

		
		PreyLogger.d("Ejecuting CameraAction Action. DONE!");
		ActionResult result = new ActionResult();
		result.setDataToSend(data);
		actionJob.finish(result);
	}

	@Override
	public boolean shouldNotify() {
		return true;
	}

	@Override
	public boolean isSyncAction() {
		return true;
	}
	
	public int getPriority(){
		return WEBCAM_PRIORITY;
	}

}
