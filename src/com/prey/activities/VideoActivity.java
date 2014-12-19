package com.prey.activities;

import java.io.IOException;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.VideoActivity;

public class VideoActivity extends Activity implements SurfaceHolder.Callback {

	  MediaRecorder recorder;
	    SurfaceHolder holder;
	    boolean recording = false;
	    public static String filePath = Environment.getExternalStorageDirectory().getPath() + "/yerp.mp4" ; 
	    public static Camera camera=null;
	    public static VideoActivity activity = null;
	    
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	    
	        recorder = new MediaRecorder();
	        initRecorder();
	        setContentView(R.layout.activity_video);
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        SurfaceView cameraView = (SurfaceView) findViewById(R.id.videoSurface);
	        holder = cameraView.getHolder();
	        holder.addCallback(this);
	        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        activity=this;
	        
	        
	        
	        
	    }
	    
	     
	    
	    private void initRecorder() {
	    	  	        
	        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
	        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
	        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	        //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
	        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	        recorder.setOutputFile(filePath);
	        //recorder.setMaxDuration(5000);  
	        //recorder.setMaxFileSize(5000000); 
	        recorder.setVideoFrameRate(30);
	        recorder.setVideoSize(720, 480);
	        	        
	    }

	    private void prepareRecorder() {
	        recorder.setPreviewDisplay(holder.getSurface());

	        try {
	            recorder.prepare();
	        } catch (IllegalStateException e) {
	            e.printStackTrace();
	            finish();
	        } catch (IOException e) {
	            e.printStackTrace();
	            finish();
	        }
	    }
	    
	    public void start( ) {
	        
	        	prepareRecorder();
	            recording = true;
	            recorder.start();
	         
	    }
	    public void stop( ) {
	         	PreyLogger.i("stop");
	            recorder.stop();
	            recording = false;
	            
	            // Let's initRecorder so we can record again
	            initRecorder();
	            
	        
	    }
	    
	     

	    public void surfaceCreated(SurfaceHolder holder) {
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int width,
	            int height) {
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	        if (recording) {
	            recorder.stop();
	            recording = false;
	        }
	        recorder.release();
	        finish();
	    }

}
