/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.wipe;

import java.io.File;

import android.os.Environment;

import com.prey.PreyLogger;

public class WipeUtil {

    public static void deleteSD() {
        String accessable = Environment.getExternalStorageState();
        PreyLogger.d("Deleting folder: " + accessable + " from SD");
        if (Environment.MEDIA_MOUNTED.equals(accessable)) {
            PreyLogger.d("accessable");
            File dir = new File(Environment.getExternalStorageDirectory() + "");
            deleteRecursive(dir);
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        PreyLogger.d("name:" + fileOrDirectory.getName());
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

}