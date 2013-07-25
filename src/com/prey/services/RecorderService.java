package com.prey.services;

 
import java.io.File;
import java.io.FileInputStream;
 
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
 
import com.prey.activities.browser.BaseBrowserActivity;
 
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyRestHttpClient;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
 
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
 
import android.os.IBinder;
 
 
import android.view.SurfaceHolder;
 

public class RecorderService extends Service {
	 
 
	private SurfaceHolder mSurfaceHolder;
	private static Camera mServiceCamera;
	public static boolean mRecordingStatus;
	private MediaRecorder mMediaRecorder;
	private File directory;
	private int cameraType=0;
	

	@Override
	public void onCreate() {
		mRecordingStatus = false;
		mServiceCamera = BaseBrowserActivity.mCamera;
 
		mSurfaceHolder = BaseBrowserActivity.mSurfaceHolder;
		

		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (mRecordingStatus == false) {
		

			startRecording();
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopRecording();
		mServiceCamera.release();
		mRecordingStatus = false;
		sendVideo(getApplicationContext());
		super.onDestroy();
	}

	public void sendVideo(Context ctx){
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		try {
			String uniqueOutFile = Environment.getExternalStorageDirectory()
					.toString() + "/videooutput.mp4";
			File file = new File(uniqueOutFile);
			
			PreyLogger.i("size:"+file.length());
		    FileInputStream fis = new FileInputStream(file);
			EntityFile entityFile = new EntityFile();
			entityFile.setFile(fis);
			entityFile.setMimeType("video/mp4");
			entityFile.setName("video.mp4");
			entityFile.setType("video");
			Map<String, String> parameters = new HashMap<String, String>();
			String URL =PreyWebServices.getFileUrlJson(ctx);
			List<EntityFile> entityFiles=new ArrayList<EntityFile>();
	        entityFiles.add(entityFile);
	        PreyHttpResponse preyHttpResponse=null;
	        preyHttpResponse=PreyRestHttpClient.getInstance(ctx).postAutentication(URL, parameters, preyConfig,entityFiles);;
	        PreyLogger.i("status line:"+preyHttpResponse.getStatusLine());
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		} 
		
	} 
	
	public boolean startRecording() {
		PreyLogger.i( "recording service starting..");
		try {
			mRecordingStatus = true;

			mServiceCamera = Camera.open(cameraType);
			mServiceCamera.setDisplayOrientation(90);
			Camera.Parameters params = mServiceCamera.getParameters();
			mServiceCamera.setParameters(params);
			Camera.Parameters p = mServiceCamera.getParameters();
			// p.set("orientation", "landscape");

			// final List<Size> listSize = p.getSupportedPreviewSizes();
			// Size mPreviewSize = listSize.get(2);
			// PreyLogger.i( "use: width = " + mPreviewSize.width
			// + " height = " + mPreviewSize.height);
			// p.setPreviewSize(320,240);
			// p.setPreviewFormat(PixelFormat.YCbCr_420_SP);

			mServiceCamera.setParameters(p);

			// try {
			// mServiceCamera.setPreviewDisplay(mSurfaceHolder);
			// mServiceCamera.startPreview();
			// }
			// catch (IOException e) {
			// Log.e(TAG, e.getMessage());
			// e.printStackTrace();
			// }

			mServiceCamera.unlock();

			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setCamera(mServiceCamera);
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
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

			mMediaRecorder.setOrientationHint(90);

			mMediaRecorder.prepare();

			mMediaRecorder.start();


			return true;
		} catch (Exception e) {
			PreyLogger.e("causa: "+e.getMessage(),e);
			e.printStackTrace();
			return false;
		}  
	}
	
	

	public void stopRecording() {
		try{
			mMediaRecorder.stop();
			mMediaRecorder.release();
		}catch(Exception e){
			
		}
		PreyLogger.i( "recording service stopped");
	}
}
