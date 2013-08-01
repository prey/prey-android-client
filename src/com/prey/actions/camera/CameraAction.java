/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.camera;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.PreyAction;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.SimpleCameraActivity;
import com.prey.exceptions.PreyException;
import com.prey.net.http.EntityFile;

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
		try {
			actionJob = actionJob2;
			AudioManager mgr = null;
			int streamType = AudioManager.STREAM_SYSTEM;

			Intent intent = new Intent(ctx, SimpleCameraActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startActivity(intent);

			int i = 0;

			mgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
			mgr.setStreamSolo(streamType, true);
			mgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			mgr.setStreamMute(streamType, true);

			while (SimpleCameraActivity.activity == null && i < 10) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				PreyLogger.i("esperando antes take [" + i + "]");
				i++;
			}

			SimpleCameraActivity.activity.takePicture();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}

			mgr.setStreamSolo(streamType, false);
			mgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			mgr.setStreamMute(streamType, false);

			try {
				i = 0;
				while (SimpleCameraActivity.activity != null && SimpleCameraActivity.dataImagen == null && i < 20) {
					Thread.sleep(1000);
					i++;
					PreyLogger.i("falta imagen[" + i + "]");
				}
			} catch (InterruptedException e) {
				PreyLogger.d("Error, causa:" + e.getMessage());
			}
			SimpleCameraActivity.activity.finish();

			HttpDataService data = null;
			if (SimpleCameraActivity.dataImagen != null) {
				PreyLogger.d("dataImagen data length=" + SimpleCameraActivity.dataImagen.length);
				InputStream file = new ByteArrayInputStream(SimpleCameraActivity.dataImagen);
				EntityFile entityFile = new EntityFile();
				entityFile.setFile(file);
				entityFile.setMimeType("image/png");
				entityFile.setName("picture.jpg");
				entityFile.setType("picture");

				data = new HttpDataService(CameraAction.DATA_ID);
				data.setList(true);
				data.addEntityFile(entityFile);
			} else {
				PreyLogger.d("dataImagen null");
			}

			ActionResult result = new ActionResult();
			result.setDataToSend(data);
			actionJob.finish(result);
		} finally {
			SimpleCameraActivity.activity = null;
			SimpleCameraActivity.dataImagen = null;
			try {
				SimpleCameraActivity.camera.stopPreview();
			} catch (Exception e) {

			}
			try {
				SimpleCameraActivity.camera.release();
			} catch (Exception e) {

			}
			SimpleCameraActivity.camera = null;
			SimpleCameraActivity.mHolder = null;
		}

	}

	private boolean existWebcamMessage(Bundle bundle) {
		boolean isWebcamMessage = false;
		for (Iterator<Map.Entry<String, String>> it = getConfig().entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			PreyLogger.d("CameraAction key:" + key + " value:" + value);
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
