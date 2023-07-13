/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.picture;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.camera.CameraAction;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.SimpleCameraActivity;
import com.prey.net.http.EntityFile;

public class PictureUtil {

    public static String FRONT = "front";
    public static String BACK = "back";
    public static HttpDataService getPicture(Context ctx) {
        HttpDataService data = null;
        int currentVolume = 0;
        AudioManager mgr = null;
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmZ");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    ) {
                int attempts = 0;
                int maximum = 4;
                mgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                currentVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
                PreyConfig.getPreyConfig(ctx).setVolume(currentVolume);
                data = new HttpDataService(CameraAction.DATA_ID);
                data.setList(true);
                do {
                    try {
                        SimpleCameraActivity.dataImagen = null;
                        PreyLogger.d("report front attempts FRONT:" + attempts);
                        byte[] frontPicture = getPicture(ctx, BACK);
                        if (frontPicture != null) {
                            PreyLogger.d("report data length front=" + frontPicture.length);
                            InputStream file = new ByteArrayInputStream(frontPicture);
                            EntityFile entityFile = new EntityFile();
                            entityFile.setFile(file);
                            entityFile.setMimeType("image/png");
                            entityFile.setFilename("picture.jpg");
                            entityFile.setName("picture");
                            entityFile.setType("image/png");
                            entityFile.setIdFile(sdf.format(new Date()) + "_" + entityFile.getType());
                            entityFile.setLength(frontPicture.length);
                            data.addEntityFile(entityFile);
                            attempts = maximum;
                        }
                    } catch (Exception e) {
                        PreyLogger.e("report error:" + e.getMessage(), e);
                    }
                    attempts++;
                } while (attempts < maximum);
                Integer numberOfCameras = SimpleCameraActivity.getNumberOfCameras(ctx);
                if (numberOfCameras != null && numberOfCameras > 1) {
                    attempts = 0;
                    do {
                        try {
                            SimpleCameraActivity.dataImagen = null;
                            PreyLogger.d("report back attempts BACK:" + attempts);
                            byte[] backPicture = getPicture(ctx, FRONT);
                            if (backPicture != null) {
                                PreyLogger.d("report data length back=" + backPicture.length);
                                InputStream file = new ByteArrayInputStream(backPicture);
                                EntityFile entityFile = new EntityFile();
                                entityFile.setFile(file);
                                entityFile.setMimeType("image/png");
                                entityFile.setFilename("screenshot.jpg");
                                entityFile.setName("screenshot");
                                entityFile.setType("image/png");
                                entityFile.setIdFile(sdf.format(new Date()) + "_" + entityFile.getType());
                                entityFile.setLength(backPicture.length);
                                data.addEntityFile(entityFile);
                                attempts = maximum;
                            }
                        } catch (Exception e) {
                            PreyLogger.e("report error:" + attempts, e);
                        }
                        attempts++;
                    } while (attempts < maximum);
                }
            }
            Intent intentCamera = new Intent(ctx, SimpleCameraActivity.class);
            intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle myKillerBundle = new Bundle();
            myKillerBundle.putInt("kill",1);
            intentCamera.putExtras(myKillerBundle);
            ctx.startActivity(intentCamera);
            ctx.sendBroadcast(new Intent(CheckPasswordHtmlActivity.CLOSE_PREY));
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage() + e.getMessage(), e);
        } finally {
            try {
                currentVolume = PreyConfig.getPreyConfig(ctx).getVolume();
                if (currentVolume > 0) {
                    mgr.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND);
                }
            } catch (Exception e) {
                PreyLogger.e("report error:" + e.getMessage(), e);
            }
        }
        return data;
    }

    private static byte[] getPicture(Context ctx, String focus) {
        AudioManager mgr = null;
        SimpleCameraActivity.dataImagen = null;
        int streamType = AudioManager.STREAM_SYSTEM;
        SimpleCameraActivity.activity = null;
        Intent intentCamera = new Intent(ctx, SimpleCameraActivity.class);
        intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentCamera.putExtra("focus", focus);
        ctx.startActivity(intentCamera);
        int i = 0;
        mgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        try {
            if (currentVolume > 0) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    mgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    mgr.setStreamMute(streamType, true);
                } else {
                    final int setVolFlags = AudioManager.FLAG_PLAY_SOUND;
                    mgr.setStreamVolume(AudioManager.STREAM_MUSIC, 0, setVolFlags);
                }
            }
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage(),e);
        }
        while (SimpleCameraActivity.activity == null&& i < 10) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                PreyLogger.e("Error sleep:" + e.getMessage(),e);
            }
            i++;
        }
        if (SimpleCameraActivity.activity != null) {
            SimpleCameraActivity.activity.takePicture(ctx);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            mgr.setStreamMute(streamType, false);
        }
        try {
            i = 0;
            while (SimpleCameraActivity.activity != null && SimpleCameraActivity.dataImagen == null && i < 9) {
                Thread.sleep(500);
                i++;
            }
        } catch (InterruptedException e) {
            PreyLogger.e("report error:" + e.getMessage(), e);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            PreyLogger.e("Error:" + e.getMessage(),e);
        }
        byte[] out=null;
        if (SimpleCameraActivity.activity != null) {
            out=SimpleCameraActivity.dataImagen;
            SimpleCameraActivity.activity.finish();
            SimpleCameraActivity.activity=null;
            SimpleCameraActivity.dataImagen=null;
        }
        try {
            currentVolume = PreyConfig.getPreyConfig(ctx).getVolume();
            if (currentVolume > 0) {
                mgr.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND);
            }
        } catch (Exception e) {
            PreyLogger.e("report error:" + e.getMessage(), e);
        }
        return out;
    }

}