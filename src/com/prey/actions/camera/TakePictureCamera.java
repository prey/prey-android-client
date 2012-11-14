package com.prey.actions.camera;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.view.Surface;
import android.view.WindowManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.net.http.EntityFile;

public class TakePictureCamera {

	public byte[] dataImagen = null;

	public HttpDataService takePicture(Context ctx) {
		PreyLogger.d("welcome TakePictureCamera");
		HttpDataService data = null;
		Camera mCamera = null;
		AudioManager mgr = null;
		int streamType = AudioManager.STREAM_SYSTEM;

		try {

			PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			if (preyConfig.isGingerbreadOrAbove()) {
				mCamera = getCameraGingerbreadOrAbove(ctx);
			} else {
				mCamera = getCamera(ctx);
			}

			if (preyConfig.isFroyoOrAbove()) {
				Parameters params = mCamera.getParameters();
				params = setParameter(ctx, params);
				mCamera.setParameters(params);
			}
			TakePictureShutterCallback shutter = new TakePictureShutterCallback();

			mgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
			mgr.setStreamSolo(streamType, true);
			mgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			mgr.setStreamMute(streamType, true);

			TakePictureCallback callback = new TakePictureCallback();
			mCamera.setPreviewDisplay(null);
			mCamera.startPreview();
			mCamera.takePicture(shutter, null, callback);
			Thread.sleep(2000);
			mCamera.stopPreview();

		} catch (Exception e) {
			PreyLogger.d("Error, causa:" + e.getMessage());
		} finally {
			if (mCamera != null)
				mCamera.release();
		}

		try {
			int i = 0;
			while (dataImagen == null && i < 20) {
				Thread.sleep(1000);
				i++;
			}
		} catch (InterruptedException e) {
			PreyLogger.d("Error, causa:" + e.getMessage());
		}

		mgr.setStreamSolo(streamType, false);
		mgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		mgr.setStreamMute(streamType, false);

		if (dataImagen != null) {
			PreyLogger.d("dataImagen data length=" + dataImagen.length);
			InputStream file = new ByteArrayInputStream(dataImagen);
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
		return data;
	}

	private Camera getCamera(Context ctx) {
		return Camera.open();
	}

	public static int CAMERA_FACING_FRONT = 1;

	@SuppressWarnings("rawtypes")
	private Camera getCameraGingerbreadOrAbove(Context ctx) {
		PreyLogger.d("getCameraGingerbreadOrAbove");
		Camera mCamera = null;
		try {

			Class noparams[] = {};
			Class clsCamera;

			clsCamera = Class.forName("android.hardware.Camera");

			Method methodGetNumberOfCameras = clsCamera.getMethod("getNumberOfCameras", noparams);
			Integer numberOfCamerasInt = (Integer) methodGetNumberOfCameras.invoke(null, null);
			// int numberOfCameras = Camera.getNumberOfCameras();

			// android.hardware.Camera.CameraInfo cameraInfo = new
			// android.hardware.Camera.CameraInfo();
			for (int camIdx = 0; numberOfCamerasInt != null && camIdx < numberOfCamerasInt.intValue(); camIdx++) {
				// Camera.getCameraInfo(camIdx, cameraInfo);
				// if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
				if (camIdx == CAMERA_FACING_FRONT) {
					try {
						Class[] param = new Class[1];
						param[0] = Integer.TYPE;
						Method methodOpen = clsCamera.getMethod("open", param);
						Integer[] input = { Integer.valueOf(camIdx) };
						mCamera = (Camera) methodOpen.invoke(null, input);
						PreyLogger.d("Camera.open(camIdx)");
						// mCamera = Camera.open(camIdx);
					} catch (RuntimeException e) {
						PreyLogger.d("Camera failed to open facing front: " + e.getMessage());
					}
				}
			}
		} catch (Exception e1) {
			PreyLogger.d("Camera failed to open facing front: " + e1.getMessage());
			mCamera = null;
		}

		if (mCamera == null) {
			mCamera = Camera.open();
		}
		return mCamera;
	}


	@TargetApi(8)
	private Parameters setParameter(Context ctx, Parameters params) {
		WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		int rotacion = windowManager.getDefaultDisplay().getRotation();
		// PreyLogger.d("Camera rotacion:"+rotacion+" [0->0 1->90 2-> 180 3->270]");
		params.setFlashMode(Parameters.FLASH_MODE_OFF);
		switch (rotacion) {
		case Surface.ROTATION_90:
			break;
		case Surface.ROTATION_180:
			params.setRotation(270);
			break;
		case Surface.ROTATION_270:
			params.setRotation(180);
			break;
		default:
			params.setRotation(90);
			break;
		}

		return params;
	}

	public class TakePictureCallback implements PictureCallback {

		public void onPictureTaken(byte[] data, Camera camera) {
			dataImagen = data;

		}
	}

	public class TakePictureShutterCallback implements ShutterCallback {

		public void onShutter() {

		}
	}
}
