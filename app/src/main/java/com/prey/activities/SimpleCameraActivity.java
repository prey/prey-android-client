/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;

public class SimpleCameraActivity extends Activity implements SurfaceHolder.Callback , OrientationManager.OrientationListener {

    public static SimpleCameraActivity activity = null;
    public static Camera camera;
    public static SurfaceHolder mHolder;
    public static byte[] dataImagen = null;
    private String focus="";
    private int rotation = 0;
    private int orientation=0;
    private int screenIntOrientation=-1;
    static final int CODE_PORTRAIT=1;
    static final int CODE_REVERSED_PORTRAIT=2;
    static final int CODE_REVERSED_LANDSCAPE=3;
    static final int CODE_LANDSCAPE=4;
    OrientationManager orientationManager=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_camera);
        Bundle extras = getIntent().getExtras();
        int kill=extras.getInt("kill");
        PreyLogger.d("Kill:"+kill);
        if(kill==1){
            finish();
        }else {
            if (extras != null) {
                focus = extras.getString("focus");
            } else {
                focus = "front";
            }
            orientationManager = new OrientationManager(getApplicationContext(), SensorManager.SENSOR_DELAY_NORMAL, this);
            orientationManager.enable();
            camera = getCamera(focus);
            if (camera != null) {
                try {
                    camera.startPreview();
                } catch (Exception e) {
                }
            }
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
            mHolder = surfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            activity = this;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Camera getCamera(String focus) {
        PreyLogger.d("getCamera(" + focus + ")");
        Camera mCamera = null;
        try {
            Class clsCamera;
            clsCamera = Class.forName("android.hardware.Camera");
            Integer numberOfCamerasInt = getNumberOfCameras();

            if (numberOfCamerasInt != null) {
                if ("front".equals(focus)) {
                    mCamera = getCamera(0, clsCamera);
                    PreyLogger.d("SimpleCameraActivity getCamera 0 focus:"+focus);
                } else {
                    mCamera = getCamera(1, clsCamera);
                    PreyLogger.d("SimpleCameraActivity getCamera 1 focus:"+focus);
                }
            }
        } catch (Exception e1) {
            PreyLogger.d("SimpleCameraActivity Camera failed to open facing front: " + e1.getMessage());
            mCamera = null;
        }
        try {
            if (mCamera == null) {
                mCamera = Camera.open();
            }
        } catch (Exception e) {
        }
        return mCamera;
    }

    public static Integer getNumberOfCameras() {
        Integer numberOfCamerasInt = null;
        try {

            Class noparams[] = {};
            Class clsCamera;
            clsCamera = Class.forName("android.hardware.Camera");
            Method methodGetNumberOfCameras = clsCamera.getMethod("getNumberOfCameras", noparams);
            numberOfCamerasInt = (Integer) methodGetNumberOfCameras.invoke(null, null);
        } catch (Exception e) {
        }
        return numberOfCamerasInt;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Camera getCamera(int idx, Class clsCamera) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Camera mCamera = null;
        try {
            Class[] param = new Class[1];
            param[0] = Integer.TYPE;
            Method methodOpen = clsCamera.getMethod("open", param);
            Integer[] input = {Integer.valueOf(idx)};
            mCamera = (Camera) methodOpen.invoke(null, input);
            PreyLogger.d("SimpleCameraActivity Camera.open(camIdx)");
            // mCamera = Camera.open(camIdx);
        } catch (RuntimeException e) {
            PreyLogger.d("SimpleCameraActivityCamera failed to open: " + e.getMessage());
        }
        return mCamera;
    }



    public void takePicture(Context ctx, String focus) {
        try {
            if (camera != null) {
                Camera.Parameters parameters = camera.getParameters();
                if (PreyConfig.getPreyConfig(ctx).isEclairOrAbove()) {
                    parameters = setParameters1(parameters);
                }
                parameters.set("iso", 400);
                if (PreyConfig.getPreyConfig(ctx).isFroyoOrAbove()) {
                    parameters = setParameters2(parameters);
                }
                camera.setParameters(parameters);
            }
        } catch (Exception e) {
            PreyLogger.e("error takePicture:"+e.getMessage(),e);
        }
        try {
            if (camera != null) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                PreyLogger.d("SimpleCameraActivity open takePicture()");
            }
        } catch (Exception e) {
        }
    }

    @TargetApi(5)
    private Camera.Parameters setParameters1(Camera.Parameters parameters) {
        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        parameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
        parameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
        return parameters;
    }

    @TargetApi(8)
    private Camera.Parameters setParameters2(Camera.Parameters parameters) {
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

            dataImagen = resizeImage(data);
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
        PreyLogger.d("SimpleCameraActivity camera setPreviewDisplay()");
        mHolder = holder;
        try {
            if (camera != null)
                camera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            PreyLogger.e("Error PreviewDisplay:" + e.getMessage(), e);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        PreyLogger.d("SimpleCameraActivity camera surfaceDestroyed()");
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

    private static final int PHOTO_HEIGHT=1024;
    private static final int PHOTO_WIDTH=768;

    byte[] resizeImage(byte[] input) {
        try{
            Bitmap original = BitmapFactory.decodeByteArray(input , 0, input.length);
            Bitmap resized = Bitmap.createScaledBitmap(original, PHOTO_WIDTH, PHOTO_HEIGHT, true);
            Bitmap resized2 =null;
            Matrix matrix = new Matrix();
            if(screenIntOrientation==CODE_PORTRAIT ){
                if ("front".equals(focus)) {
                    matrix.postRotate(90);
                } else {
                    matrix.postRotate(270);
                }
            }
            if(screenIntOrientation==CODE_REVERSED_PORTRAIT){
                if ("front".equals(focus)) {
                    matrix.postRotate(270);
                } else {
                    matrix.postRotate(90);
                }
            }
            if(screenIntOrientation==CODE_LANDSCAPE ){
                if ("front".equals(focus)) {
                    matrix.postRotate(0);
                } else {
                    matrix.postRotate(0);
                }
            }
            if(screenIntOrientation==CODE_REVERSED_LANDSCAPE ){
                if ("front".equals(focus)) {
                    matrix.postRotate(180);
                } else {
                    matrix.postRotate(180);
                }
            }
            resized2 = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), matrix, true);
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            resized2.compress(Bitmap.CompressFormat.JPEG, 100, blob);
            return blob.toByteArray();
        } catch (Exception e) {
            return input;
        }
    }

    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        switch(screenOrientation){
            case PORTRAIT:
                screenIntOrientation=1;
                break;
            case REVERSED_PORTRAIT:
                screenIntOrientation=2;
                break;
            case REVERSED_LANDSCAPE:
                screenIntOrientation=3;
                break;
            case LANDSCAPE:
                screenIntOrientation=4;
                break;
        }
    }
}

