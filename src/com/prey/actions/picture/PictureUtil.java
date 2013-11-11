package com.prey.actions.picture;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.camera.CameraAction;
import com.prey.activities.SimpleCameraActivity;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyRestHttpClient;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;

public class PictureUtil {

	public static HttpDataService getPicture(Context ctx) {
		HttpDataService data = null;
		AudioManager mgr = null;
		int streamType = AudioManager.STREAM_SYSTEM;
		try {
			SimpleCameraActivity.activity = null;
			Intent intent = new Intent(ctx, SimpleCameraActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startActivity(intent);

			int i = 0;

			mgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
			mgr.setStreamSolo(streamType, true);
			mgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			mgr.setStreamMute(streamType, true);

			while (SimpleCameraActivity.activity == null && i < 20) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				PreyLogger.i("esperando antes take picture[" + i + "]");
				i++;
			}
			if (SimpleCameraActivity.activity != null) {
				PreyLogger.i("takePicture activity no nulo");
				SimpleCameraActivity.activity.takePicture();
			} else {
				PreyLogger.i("takePicture activity nulo");
			}
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
				PreyLogger.i("Error, causa:" + e.getMessage());
			}
			if (SimpleCameraActivity.activity != null) {
				SimpleCameraActivity.activity.finish();
			}

			if (SimpleCameraActivity.dataImagen != null) {
				PreyLogger.i("dataImagen data length=" + SimpleCameraActivity.dataImagen.length);
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
				PreyLogger.i("dataImagen null");
			}

		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
		return data;
	}

	

}