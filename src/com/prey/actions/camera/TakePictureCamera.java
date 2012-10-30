package com.prey.actions.camera;

 

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.view.Surface;
import android.view.WindowManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

import com.prey.actions.HttpDataService; 
import com.prey.actions.PreyExecutionWaitNotify;

public class TakePictureCamera {

	public byte[] dataImagen = null;

	public HttpDataService takePicture(Context ctx) {
		PreyLogger.d("welcome TakePictureCamera");
		HttpDataService data =null;
		Camera mCamera = null;
		//PreyExecutionWaitNotify waitNotifyCamera=null;
		try {
			

			mCamera=getCamera(ctx);
			Parameters params = mCamera.getParameters();
			params=setParameter(ctx, params);
			mCamera.setParameters(params);

			//waitNotifyCamera = new PreyExecutionWaitNotify();
			
 
			 TakePictureShutterCallback shutter=new TakePictureShutterCallback(ctx);
			
			//AudioManager mgr = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
			// int streamType = AudioManager.STREAM_SYSTEM;
			//mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
 

		    
		    
			TakePictureCallback callback=new TakePictureCallback();
			mCamera.reconnect();
			mCamera.setPreviewDisplay(null);
			mCamera.startPreview();
			mCamera.takePicture(shutter, null, callback);
			Thread.sleep(2000);			
			mCamera.stopPreview();
			//mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
		} catch (Exception e) {
			PreyLogger.d("Error, causa:" + e.getMessage());
		} finally {
			if (mCamera!=null)
				mCamera.release();
		}
		//waitNotifyCamera.doWait();
	  
	    
		 
		try {
			int i = 0;
			while (dataImagen == null && i < 10) {
			Thread.sleep(2000);
				i++;
			}
		} catch (InterruptedException e) {
			PreyLogger.d("Error, causa:" + e.getMessage());
		}  
		if (dataImagen != null) {
			PreyLogger.d("dataImagen data length=" + dataImagen.length);			
			InputStream input = new ByteArrayInputStream(dataImagen);			
			data = new HttpDataService(CameraAction.DATA_ID);
			data.setList(true);
			data.addFile(input);
		} else {
			PreyLogger.d("dataImagen nulo");
		}
		return data;
	}
	
	

	
 
	private Camera getCamera(Context ctx){
		Camera mCamera =null;
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		if (preyConfig.isGingerbreadOrAbove()) {
			int numberOfCameras = Camera.getNumberOfCameras();
			CameraInfo cameraInfo = new CameraInfo();
			for (int camIdx = 0; camIdx < numberOfCameras; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo);
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
					try {
						mCamera = Camera.open(camIdx);
					} catch (RuntimeException e) {
						PreyLogger.d("Camera failed to open facing front: "+ e.getLocalizedMessage());
					}
				}
			}
		}
		if (mCamera == null) {
			mCamera = Camera.open();
		}
		return mCamera;
	}
	
	private Parameters setParameter(Context ctx,Parameters params){
		WindowManager windowManager = (WindowManager)  ctx.getSystemService(Context.WINDOW_SERVICE);
		int rotacion=windowManager.getDefaultDisplay().getRotation();
		//PreyLogger.d("Camera rotacion:"+rotacion+" [0->0 1->90 2-> 180 3->270]");
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
	
	public class TakePictureShutterCallback implements ShutterCallback{
	 
		private Context ctx=null;
		 
		
		public TakePictureShutterCallback(Context ctx ){
			this.ctx=ctx;
		 
		}
		
		public void onShutter() {
			 
			 AudioManager mgr = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
			 	mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
			//	waitNotifyCamera.doNotify();
			 
		}		
	}
}
