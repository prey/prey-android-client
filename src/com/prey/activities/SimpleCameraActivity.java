package com.prey.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
 

import android.content.res.Configuration;
import android.hardware.Camera;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
 
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.prey.PreyLogger;
import com.prey.R;

public class SimpleCameraActivity extends Activity implements SurfaceHolder.Callback {

	public static SimpleCameraActivity activity = null;
	public static Camera camera;

	public static SurfaceHolder mHolder;
	public static byte[] dataImagen = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_camera);
		
		Bundle extras=getIntent().getExtras();
		String focus=null;
		if(extras!=null){
			focus=extras.getString("focus");
		}else{
			focus="front";
		}
		PreyLogger.d("focus:"+focus);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		mHolder = surfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		try {
			int numberOfCameras = Camera.getNumberOfCameras();
			if (numberOfCameras == 1) {
				camera = Camera.open();
				PreyLogger.d("open camera()");
			} else {
				if ("front".equals(focus)){
					camera = Camera.open(0);
					PreyLogger.d("open camera(0)");
				} else {
					camera = Camera.open(1);
					PreyLogger.d("open camera(1)");
				}
			}
		} catch(java.lang.NoSuchMethodError ne){	
		} catch (Exception e) {
		}
		if (camera==null){
			try {
				camera = Camera.open(0); 
			} catch (Exception e) {
			}
		}
		camera.startPreview();
		activity = this;
	}
	

	@SuppressLint("NewApi")
	public void takePicture(String focus) {
		try {
			if (camera != null) {
				Camera.Parameters parameters = camera.getParameters();
				if("front".equals(focus)){
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						parameters.set("orientation", "portrait");
						parameters.set("rotation", 90);
					}
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						parameters.set("orientation", "landscape");
						parameters.set("rotation", 180);
					}
				}else{
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						parameters.set("orientation", "portrait");
						parameters.set("rotation", 270);
					}
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						parameters.set("orientation", "landscape");
						parameters.set("rotation", 0);
					}
				}
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				parameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
				parameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
				//parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
				parameters.set("iso", 400);
				parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
				
				camera.setParameters(parameters);
				
			}
		} catch (Exception e) {
		}	
		
		try {	
			if(camera!=null){
			
				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				PreyLogger.d("open takePicture()");
			}
		} catch (Exception e) {
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			dataImagen = data;
		}
	};

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if(camera != null) {
    		Camera.Parameters parameters = camera.getParameters();
    		camera.setParameters(parameters);
    		camera.startPreview();
    	}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		PreyLogger.d("camera setPreviewDisplay()");
		mHolder = holder;
		try {
			if (camera != null)
				camera.setPreviewDisplay(mHolder);
		} catch (Exception e) {
			PreyLogger.e("Error PreviewDisplay:" + e.getMessage(), e);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		PreyLogger.d("camera surfaceDestroyed()");
		if (camera != null) {
			try{
				camera.stopPreview();
			}catch(Exception e){
			}
			try{
				camera.release();
			}catch(Exception e){
			}
			camera = null;
		}
	}
	
	
	  
}
