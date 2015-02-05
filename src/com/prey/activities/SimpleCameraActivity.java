package com.prey.activities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.Activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;

public class SimpleCameraActivity extends Activity implements SurfaceHolder.Callback {

	public static SimpleCameraActivity activity = null;
	public static Camera camera;

	public static SurfaceHolder mHolder;
	public static byte[] dataImagen = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_camera);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Bundle extras = getIntent().getExtras();
		String focus = null;
		if (extras != null) {
			focus = extras.getString("focus");
		} else {
			focus = "front";
		}
		camera=getCamera(focus);
		if (camera != null) {
			try {
				camera.startPreview();
			} catch (Exception e) {
			}
		}
		PreyLogger.d("focus:" + focus);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		mHolder = surfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		activity = this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Camera getCamera(String focus) {
		PreyLogger.d("getCamera("+focus+")");
		Camera mCamera = null;
		try {
			Class clsCamera;
			clsCamera = Class.forName("android.hardware.Camera");
			Integer numberOfCamerasInt = getNumberOfCameras();
			if (numberOfCamerasInt != null) {
				if ("front".equals(focus)) {
					mCamera = getCamera(0, clsCamera);
				} else {
					mCamera = getCamera(1, clsCamera);
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
	
	public static  Integer getNumberOfCameras(){
		Integer numberOfCamerasInt =null;
		try{
		
		Class noparams[] = {};
		Class clsCamera;
		clsCamera = Class.forName("android.hardware.Camera");
		Method methodGetNumberOfCameras = clsCamera.getMethod("getNumberOfCameras", noparams);
		numberOfCamerasInt = (Integer) methodGetNumberOfCameras.invoke(null, null);
		} catch (Exception e) {
		}
		return numberOfCamerasInt;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Camera getCamera(int idx, Class clsCamera) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		Camera mCamera = null;
		try {
			Class[] param = new Class[1];
			param[0] = Integer.TYPE;
			Method methodOpen = clsCamera.getMethod("open", param);
			Integer[] input = { Integer.valueOf(idx) };
			mCamera = (Camera) methodOpen.invoke(null, input);
			PreyLogger.d("Camera.open(camIdx)");
			// mCamera = Camera.open(camIdx);
		} catch (RuntimeException e) {
			PreyLogger.d("Camera failed to open: " + e.getMessage());
		}
		return mCamera;
	}



	public void takePicture(Context ctx,String focus) {
		try {
			if (camera != null) {
				Camera.Parameters parameters = camera.getParameters();
				if ("front".equals(focus)) {
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						parameters.set("orientation", "portrait");
						parameters.set("rotation", 90);
					}
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						parameters.set("orientation", "landscape");
						parameters.set("rotation", 180);
					}
				} else {
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						parameters.set("orientation", "portrait");
						parameters.set("rotation", 270);
					}
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						parameters.set("orientation", "landscape");
						parameters.set("rotation", 0);
					}
				}
				if(PreyConfig.getPreyConfig(ctx).isEclairOrAbove()){
					parameters=setParameters1(parameters);
				}
				// parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
				parameters.set("iso", 400);
				if(PreyConfig.getPreyConfig(ctx).isFroyoOrAbove()){
					parameters=setParameters2(parameters);
				}

				camera.setParameters(parameters);

			}
		} catch (Exception e) {
		}

		try {
			if (camera != null) {

				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				PreyLogger.d("open takePicture()");
			}
		} catch (Exception e) {
		}
	}
	
	@TargetApi(5)
	private Camera.Parameters setParameters1(Camera.Parameters parameters){
		parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		parameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
		parameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
		return parameters;
	}
	
	@TargetApi(8)
	private Camera.Parameters setParameters2(Camera.Parameters parameters){
		parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
		return parameters;
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
		if (camera != null) {
			try {
				Camera.Parameters parameters = camera.getParameters();
				camera.setParameters(parameters);
			} catch (Exception e) {
			}
			try {
				camera.startPreview();
			} catch (Exception e) {
			}
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
			try {
				camera.stopPreview();
			} catch (Exception e) {
			}
			try {
				camera.release();
			} catch (Exception e) {
			}
			camera = null;
		}
	}

}
