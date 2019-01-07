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
import android.support.v4.app.ActivityCompat;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.camera.CameraAction;

import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.SimpleCameraActivity;
import com.prey.net.http.EntityFile;

public class PictureUtil {

    public static HttpDataService getPicture(Context ctx) {
        HttpDataService data = null;
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmZ");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    ) {
                byte[] frontPicture = getPicture(ctx, "front");
                data = new HttpDataService(CameraAction.DATA_ID);
                data.setList(true);
                if (frontPicture != null) {
                    PreyLogger.d("front data length=" + frontPicture.length);
                    InputStream file = new ByteArrayInputStream(frontPicture);
                    EntityFile entityFile = new EntityFile();
                    entityFile.setFile(file);
                    entityFile.setMimeType("image/png");
                    entityFile.setName("picture.jpg");
                    entityFile.setType("picture");
                    entityFile.setIdFile(sdf.format(new Date()) + "_" + entityFile.getType());
                    entityFile.setLength(frontPicture.length);
                    data.addEntityFile(entityFile);
                }
                Integer numberOfCameras = SimpleCameraActivity.getNumberOfCameras();
                if (numberOfCameras != null && numberOfCameras > 1) {
                    //Thread.sleep(6000);
                    byte[] backPicture = getPicture(ctx, "back");
                    if (backPicture != null) {
                        PreyLogger.d("back data length=" + backPicture.length);
                        InputStream file = new ByteArrayInputStream(backPicture);
                        EntityFile entityFile = new EntityFile();
                        entityFile.setFile(file);
                        entityFile.setMimeType("image/png");
                        entityFile.setName("screenshot.jpg");
                        entityFile.setType("screenshot");
                        entityFile.setIdFile(sdf.format(new Date()) + "_" + entityFile.getType());
                        entityFile.setLength(backPicture.length);
                        data.addEntityFile(entityFile);
                    }
                }
            }
            Intent intent2 = new Intent(ctx, SimpleCameraActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle myKillerBundle = new Bundle();
            myKillerBundle.putInt("kill",1);
            intent2.putExtras(myKillerBundle);
            ctx.startActivity(intent2);
            ctx.sendBroadcast(new Intent(CheckPasswordHtmlActivity.CLOSE_PREY));

        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage() + e.getMessage(), e);
        }
        return data;
    }

    private static byte[] getPicture(Context ctx, String focus) {
        AudioManager mgr = null;
        SimpleCameraActivity.dataImagen = null;
        int streamType = AudioManager.STREAM_SYSTEM;
        SimpleCameraActivity.activity = null;
        Intent intent = new Intent(ctx, SimpleCameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("focus", focus);
        ctx.startActivity(intent);
        int i = 0;
        mgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        //mgr.setStreamSolo(streamType, true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            mgr.setStreamMute(streamType, true);
        }else{
            final int setVolFlags = AudioManager.FLAG_PLAY_SOUND;
            mgr.setStreamVolume(AudioManager.STREAM_MUSIC, 0, setVolFlags);
        }

        while (SimpleCameraActivity.activity == null&& i < 10) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            i++;
        }
        if (SimpleCameraActivity.activity != null) {
            SimpleCameraActivity.activity.takePicture(ctx,focus);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        //mgr.setStreamSolo(streamType, false);
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
            PreyLogger.e("Error:" + e.getMessage(),e);
        }
        byte[] out=null;
        if (SimpleCameraActivity.activity != null) {
            out=SimpleCameraActivity.dataImagen;
            SimpleCameraActivity.activity.finish();
            SimpleCameraActivity.activity=null;
            SimpleCameraActivity.dataImagen=null;

        }
        return out;
    }

}