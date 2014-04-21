package com.prey.activities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.picture.Preview;
import com.prey.actions.picture.Preview2;
 

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
 
public class SimpleCameraActivity2  extends Activity {

	private static final String TAG = "CamTestActivity";
	Preview preview;
	Button buttonClick;
	public static Camera camera;
	String fileName;
	Activity act;
	Context ctx;
	public static SimpleCameraActivity2 activity = null;
	public static byte[] dataImagen = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		act = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.simple_camera2);
		SurfaceView surfaceView=(SurfaceView)findViewById(R.id.surfaceView);
		preview = new Preview(this,surfaceView );
		preview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		((FrameLayout) findViewById(R.id.preview)).addView(preview);
		preview.setKeepScreenOn(true);
		
		Bundle extras=getIntent().getExtras();
		String focus=null;
		if(extras!=null){
			focus=extras.getString("focus");
		}else{
			focus="front";
		}
		PreyLogger.d("focus:"+focus);
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
		if (camera!=null){
			PreyLogger.i("_________1 CAMERA NOT NULL______");
		}
		
	
		
		preview.setCamera(camera);
		camera.startPreview();
		activity = this;
		if (camera!=null){
			PreyLogger.i("_________2 CAMERA NOT NULL______");
		}
		 
	}
	
	public void takePicture(String focus){
		
		
		try {
			if (camera != null) {
				if (camera!=null){
					PreyLogger.i("_________3 CAMERA NOT NULL______");
				}
				
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
			PreyLogger.i("Error, m:"+e.getMessage());
		}	
		
		
		camera.takePicture(shutterCallback, rawCallback, jpegCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//      preview.camera = Camera.open();
	//	camera = Camera.open(1);
	//	camera.startPreview();
			}

	@Override
	protected void onPause() {
 
		super.onPause();
	}

 

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// Log.d(TAG, "onPictureTaken - raw");
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			dataImagen=data;
		}
	};
	protected void onDestroy() {
		super.onDestroy();
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
}
