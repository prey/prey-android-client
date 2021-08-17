/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.R;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.backwardcompatibility.AboveCupcakeSupport;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class PreReportActivity extends Activity implements SurfaceHolder.Callback, OrientationManager.OrientationListener {

    public static PreReportActivity activity = null;
    public static Camera camera;
    public static SurfaceHolder mHolder;
    public static byte[] dataImagen = null;
    String focus = "";
    boolean firstPicture = false;
    boolean secondPicture = false;
    static String BACK = "back";
    static String FRONT = "front";
    static final int CODE_PORTRAIT=1;
    static final int CODE_REVERSED_PORTRAIT=2;
    static final int CODE_REVERSED_LANDSCAPE=3;
    static final int CODE_LANDSCAPE=4;
    OrientationManager orientationManager=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_camera2);
        orientationManager = new OrientationManager(getApplicationContext(), SensorManager.SENSOR_DELAY_NORMAL, this);
        orientationManager.enable();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
        TextView pre_report_title = (TextView) findViewById(R.id.pre_report_title);
        pre_report_title.setTypeface(titilliumWebBold);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new CameraTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new CameraTask().execute();
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
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        try {
            if (camera != null) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                PreyLogger.d("PreReportActivity open takePicture()");
            }
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
    }

    @TargetApi(5)
    private Camera.Parameters setParameters1(Camera.Parameters parameters) {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        return parameters;
    }

    @TargetApi(8)
    private Camera.Parameters setParameters2(Camera.Parameters parameters) {
        float progress =0.5f;
        int min = parameters.getMinExposureCompensation(); // -3 on my phone
        int max = parameters.getMaxExposureCompensation(); // 3 on my phone
        float realProgress = progress - 0.5f;
        int value;
        if (realProgress < 0) {
            value = -(int) (realProgress * 2 * min);
        } else {
            value = (int) (realProgress * 2 * max);
        }
        PreyLogger.d("setExposureCompensation value:"+value);
        PreyLogger.d("setExposureCompensation   max:"+parameters.getMaxExposureCompensation());
        parameters.setExposureCompensation(value);//parameters.getMaxExposureCompensation());
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

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            PreyLogger.d("PreReportActivity camera jpegCallback");
            dataImagen = resizeImage(data);
            try {
                String path = getExternalFilesDir(null).toString()  + "/Prey/";
                PreyLogger.d("PreReportActivity path:" + path);
                try {
                    new File(path).mkdir();
                } catch (Exception e) {
                    PreyLogger.e("Error:"+e.getMessage(),e);
                }
                File file = new File(path + focus + ".jpg");
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(dataImagen);
                bos.flush();
                bos.close();
                if (FRONT.equals(focus)) {
                    firstPicture = true;
                } else {
                    secondPicture = true;
                }
            } catch (Exception e) {
                PreyLogger.e("PreReportActivity camera jpegCallback err" + e.getMessage(), e);
            }
        }
    };

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                camera.setParameters(parameters);
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            try {
                camera.startPreview();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        PreyLogger.d("PreReportActivity camera setPreviewDisplay()");
        mHolder = holder;
        try {
            if (camera != null)
                camera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            PreyLogger.e("Error PreviewDisplay:" + e.getMessage(), e);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        PreyLogger.d("PreReportActivity camera surfaceDestroyed()");
        if (camera != null) {
            try {
                camera.stopPreview();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            try {
                camera.release();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            camera = null;
        }
    }

    private static final int PHOTO_HEIGHT = 1024;
    private static final int PHOTO_WIDTH = 768;
    private int screenIntOrientation=-1;

    byte[] resizeImage(byte[] input) {
        try {
            Bitmap original = BitmapFactory.decodeByteArray(input , 0, input.length);
            Bitmap resized = Bitmap.createScaledBitmap(original, PHOTO_WIDTH, PHOTO_HEIGHT, true);
            Bitmap resized2 =null;
            Matrix matrix = new Matrix();
            PreyLogger.d("SimpleCameraActivity focus :"+focus+" screenIntOrientation:"+screenIntOrientation);
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

    private class CameraTask extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog = null;
        PreyLocation preyLocation = null;
        @Override
        protected void onPreExecute() {
            PreyLogger.d("PreReportActivity antes camera");
            try {
                progressDialog = new ProgressDialog(PreReportActivity.this);
                progressDialog.setMessage(getApplicationContext().getText(R.string.pre_report_camera1).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            } catch (Exception e) {
            }
        }
        @Override
        protected Void doInBackground(String... data) {
            try {
                firstPicture = false;
                secondPicture = false;
                String path = getExternalFilesDir(null).toString()  + "/Prey/";
                File file1 = new File(path + "" + FRONT + ".jpg");
                file1.delete();
                File file2 = new File(path + "" + BACK + ".jpg");
                file2.delete();
                File file3 = new File(path + "map.jpg");
                file3.delete();
                focus = FRONT;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(0, cameraInfo);
                camera = Camera.open(0);
                if (camera != null) {
                    try {
                        camera.setPreviewDisplay(mHolder);
                        camera.startPreview();
                    } catch (Exception e) {
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                }
                takePicture(getApplicationContext(), focus);
                int i = 0;
                while (i < 20 && !firstPicture) {
                    Thread.sleep(500);
                    i++;
                }
                camera.stopPreview();
                camera.release();
                PreyLogger.d("PreReportActivity Camera 1 size:" + (dataImagen == null ? -1 : dataImagen.length));
            } catch (Exception e) {
                PreyLogger.e("Camera 1 error:" + e.getMessage(), e);
            }
            try {
                Thread.sleep(2000);
                try {
                    if (progressDialog != null)
                        progressDialog.setMessage(getApplicationContext().getText(R.string.pre_report_camera2).toString());
                } catch (Exception e) {
                    PreyLogger.e("Error:"+e.getMessage(),e);
                }
                focus = BACK;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(1, cameraInfo);
                camera = Camera.open(1);
                if (camera != null) {
                    try {
                        camera.setPreviewDisplay(mHolder);
                        camera.startPreview();
                    } catch (Exception e) {
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                }
                takePicture(getApplicationContext(), focus);
                int i = 0;
                while (i < 20 && !secondPicture) {
                    Thread.sleep(500);
                    i++;
                }
                PreyLogger.d("PreReportActivity Camera 2 size:" + (dataImagen == null ? -1 : dataImagen.length));
            } catch (Exception e) {
                PreyLogger.e("Camera 2 error:" + e.getMessage(), e);
            }
            try {
                if (progressDialog != null)
                    progressDialog.setMessage(getApplicationContext().getText(R.string.pre_report_location).toString());
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            try{
                preyLocation = LocationUtil.getLocation(getApplicationContext(), null, true);
                if (preyLocation != null && preyLocation.getLocation()!=null && preyLocation.getLocation().getLatitude() != 0 && preyLocation.getLocation().getLongitude() !=0) {
                    PreyConfig.getPreyConfig(getApplicationContext()).setLocation(preyLocation);
                }else{
                    preyLocation=PreyConfig.getPreyConfig(getApplicationContext()).getLocation();
                }
            } catch (Exception e) {
                PreyLogger.e("error location:" + e.getMessage(), e);
            }
            try {
                if (progressDialog != null)
                    progressDialog.setMessage(getApplicationContext().getText(R.string.pre_report_public_ip).toString());
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            try{
                PreyPhone phone = new PreyPhone(getApplicationContext());
                String publicIp = phone.getIPAddress();
                PreyConfig.getPreyConfig(getApplicationContext()).setPublicIp(publicIp);
                com.prey.PreyPhone.Wifi wifiPhone = phone.getWifi();
                String ssid = wifiPhone.getSsid() == null ? "" : wifiPhone.getSsid();
                String model = Build.MODEL;
                String vendor = "Google";
                try {
                    vendor = AboveCupcakeSupport.getDeviceVendor();
                } catch (Exception e) {
                    PreyLogger.e("Error:"+e.getMessage(),e);
                }
                String imei = phone.getHardware().getSerialNumber();
                PreyConfig.getPreyConfig(getApplicationContext()).setSsid(ssid);
                PreyConfig.getPreyConfig(getApplicationContext()).setImei(imei);
                PreyConfig.getPreyConfig(getApplicationContext()).setModel(model + " " + vendor);
            } catch (Exception e) {
                PreyLogger.e("error public_ip:" + e.getMessage(), e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void unused) {
            PreyLogger.d("PreReportActivity post camera");
            try {
                if (progressDialog != null)
                    progressDialog.dismiss();
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
            if (preyLocation != null) {
                intent.putExtra("lat", preyLocation.getLat());
                intent.putExtra("lng", preyLocation.getLng());
            }
            startActivity(intent);
            finish();
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