/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.camera;

import java.util.Iterator;
import java.util.Map;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.PreyAction;
import com.prey.actions.PreyStatus;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.CameraActivity;
import com.prey.exceptions.PreyException;

public class CameraAction extends PreyAction {

	public static final String DATA_ID = "webcam";

	public static ActionJob actionJob;

	public CameraAction() {
		PreyLogger.d("Ejecuting CameraAction Action");

	}

	@Override
	public String textToNotifyUserOnEachReport(Context ctx) {
		return "";
	}

	@Override
	public void execute(ActionJob actionJob2, Context ctx) throws PreyException {

		actionJob = actionJob2;
		Bundle bundle = new Bundle();
		boolean isWebcamMessage = existWebcamMessage(bundle);
		
		
		if (isWebcamMessage) {
			PreyStatus.getInstance().setTakenPicture(false);
			Intent popup = new Intent(ctx, CameraActivity.class);
			popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			popup.putExtras(bundle);
			ctx.startActivity(popup);
			try {
				int i = 0;
				while (!PreyStatus.getInstance().isTakenPicture() && i < 30) {
					Thread.sleep(2000);
					i++;
				}
			} catch (InterruptedException e) {
				PreyLogger.d("Error, causa:" + e.getMessage());
			}  
		}  

		TakePictureCamera takePictureCamera = new TakePictureCamera();
		HttpDataService data = takePictureCamera.takePicture(ctx);
		PreyLogger.d("Ejecuting CameraAction Action. DONE!");
		ActionResult result = new ActionResult();
		result.setDataToSend(data);
		actionJob.finish(result);
		
	}

	private boolean existWebcamMessage(Bundle bundle){
		boolean isWebcamMessage=false;
		for (Iterator<Map.Entry<String, String>> it = getConfig().entrySet()
				.iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			PreyLogger.d("CameraAction key:"+key+" value:"+value);
			bundle.putString(key, value);
			if ("webcam_message".equals(key) && !"".equals(key)) {
				isWebcamMessage = true;
			}
		}
		return isWebcamMessage;
	}
	@Override
	public boolean shouldNotify() {
		return true;
	}

	@Override
	public boolean isSyncAction() {
		return true;
	}

	public int getPriority() {
		return WEBCAM_PRIORITY;
	}


}
