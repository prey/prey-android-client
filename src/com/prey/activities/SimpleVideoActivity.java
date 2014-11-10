package com.prey.activities;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

import android.hardware.Camera;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyRestHttpClient;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;

public class SimpleVideoActivity extends Activity implements
		SurfaceHolder.Callback {

	public static SimpleVideoActivity activity = null;

	private static Camera mServiceCamera;

	private MediaRecorder mMediaRecorder;
	private SurfaceHolder mSurfaceHolder;
	public static byte[] dataImagen = null;

	private File directory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_camera);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		activity = null;

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		mSurfaceHolder = surfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		try {
			
			Context ctx=getApplicationContext();
			PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			if (preyConfig.isGingerbreadOrAbove()) {
				mServiceCamera = getCameraGingerbreadOrAbove(ctx);
			} else {
				mServiceCamera = getCamera(ctx);
			}
			 

		} catch (Exception e) {
			PreyLogger.e("Error open camera:" + e.getMessage(), e);

		}
		 
		activity = this;

	}

	public void sendVideo(Context ctx) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		try {
			String uniqueOutFile = Environment.getExternalStorageDirectory()
					.toString() + "/videooutput.mp4";
			File file = new File(uniqueOutFile);

			PreyLogger.i("size:" + file.length());
			FileInputStream fis = new FileInputStream(file);
			EntityFile entityFile = new EntityFile();
			entityFile.setFile(fis);
			entityFile.setMimeType("video/mp4");
			entityFile.setName("video.mp4");
			entityFile.setType("video");
			Map<String, String> parameters = new HashMap<String, String>();
			String URL = PreyWebServices.getInstance().getFileUrlJson(ctx);
			List<EntityFile> entityFiles = new ArrayList<EntityFile>();
			entityFiles.add(entityFile);
			PreyHttpResponse preyHttpResponse = null;
			preyHttpResponse = PreyRestHttpClient
					.getInstance(ctx)
					.postAutentication(URL, parameters, preyConfig, entityFiles);
			;
			PreyLogger.i("status line:" + preyHttpResponse.getStatusLine());
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}

	}

	public void takeVideo() {
		try {

			// mServiceCamera.setDisplayOrientation(90);
			Camera.Parameters params = mServiceCamera.getParameters();
			mServiceCamera.setParameters(params);
			Camera.Parameters p = mServiceCamera.getParameters();

			mServiceCamera.setParameters(p);

			// mServiceCamera.unlock();

			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setCamera(mServiceCamera);
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
			//mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

			directory = new File(Environment.getExternalStorageDirectory()
					.toString() + "/");
			if (!directory.exists())
				directory.mkdirs();

			String uniqueOutFile = Environment.getExternalStorageDirectory()
					.toString() + "/videooutput.mp4";
			File outFile = new File(directory, uniqueOutFile);
			if (outFile.exists()) {
				outFile.delete();
			}

			mMediaRecorder.setOutputFile(uniqueOutFile);

			// mMediaRecorder.setVideoFrameRate(80);
			mMediaRecorder.setVideoSize(320, 240);
			mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
			mMediaRecorder.setMaxDuration(20000); // 20 seconds
			mMediaRecorder.setMaxFileSize(2000000); // Approximately 2 megabytes
			// mMediaRecorder.setOrientationHint(90);

			mMediaRecorder.prepare();

			mMediaRecorder.start();

		} catch (Exception e) {
			PreyLogger.e("causa: " + e.getMessage(), e);

		}

	}

	public void stopRecording() {
		try {
			mMediaRecorder.stop();
			mMediaRecorder.release();

		} catch (Exception e) {

		}
		try {
			if (mServiceCamera != null) {
				mServiceCamera.stopPreview();
				mServiceCamera.release();
				mServiceCamera = null;
			}
		} catch (Exception e) {

		}
		PreyLogger.i("recording service stopped");
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	protected void onDestroy() {
		super.onDestroy();

	}

	public void surfaceCreated(SurfaceHolder holder) {
		PreyLogger.i("camera setPreviewDisplay()");
		mSurfaceHolder = holder;
		try {
			if (mServiceCamera != null)
				mServiceCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (Exception e) {
			PreyLogger.e("Error PreviewDisplay:" + e.getMessage(), e);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		PreyLogger.d("camera surfaceDestroyed()");
		if (mServiceCamera != null) {
			mServiceCamera.stopPreview();
			mServiceCamera.release();
			mServiceCamera = null;
		}
	}
	
	private Camera getCamera(Context ctx) {
		return Camera.open();
	}

	 

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

		//	android.hardware.Camera.CameraInfo cameraInfo = new
		//	 android.hardware.Camera.CameraInfo();
			if ( numberOfCamerasInt != null){
				if (numberOfCamerasInt==1){
					mCamera=getCamera(0,clsCamera);
				}
				else{
					mCamera=getCamera(1,clsCamera);
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
	
	@SuppressWarnings("rawtypes")
	private Camera getCamera(int idx,Class clsCamera) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException{
		Camera mCamera=null;
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
}
