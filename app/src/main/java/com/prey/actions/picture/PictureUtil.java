/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.picture;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.camera.CameraAction;

import com.prey.activities.SimpleCameraActivity;
import com.prey.net.http.EntityFile;

public class PictureUtil {

    public static HttpDataService getPicture(Context ctx) {
        HttpDataService data = null;
        try {
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
                entityFile.setLength(frontPicture.length);
                data.addEntityFile(entityFile);
            }
            Integer numberOfCameras = SimpleCameraActivity.getNumberOfCameras();
            if (numberOfCameras!=null&&numberOfCameras > 1) {
                Thread.sleep(6000);
                byte[] backPicture = getPicture(ctx, "back");
                if (backPicture != null) {
                    PreyLogger.d("back data length=" + backPicture.length);
                    InputStream file = new ByteArrayInputStream(backPicture);
                    EntityFile entityFile = new EntityFile();
                    entityFile.setFile(file);
                    entityFile.setMimeType("image/png");
                    entityFile.setName("screenshot.jpg");
                    entityFile.setType("screenshot");
                    entityFile.setLength(backPicture.length);
                    data.addEntityFile(entityFile);
                }
            }
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("focus", focus);
        ctx.startActivity(intent);
        int i = 0;
        mgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamSolo(streamType, true);
        mgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        mgr.setStreamMute(streamType, true);
        while (SimpleCameraActivity.activity == null&& i < 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            i++;
        }
        if (SimpleCameraActivity.activity != null) {
            SimpleCameraActivity.activity.takePicture(ctx,focus);
        }
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
        }
        mgr.setStreamSolo(streamType, false);
        mgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        mgr.setStreamMute(streamType, false);
        try {
            i = 0;
            while (SimpleCameraActivity.activity != null && SimpleCameraActivity.dataImagen == null && i < 5) {
                Thread.sleep(2000);
                i++;
            }
        } catch (InterruptedException e) {
            PreyLogger.i("Error:" + e.getMessage());
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