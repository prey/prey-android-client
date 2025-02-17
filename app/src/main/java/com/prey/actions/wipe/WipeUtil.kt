/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.wipe

import android.os.Environment

import com.prey.PreyLogger

import java.io.File

/**
 * Utility class for wiping data from the device's external storage.
 */
object WipeUtil {

    /**
     * Deletes all data from the device's external storage.
     */
    fun deleteSD() {
        val accessable = Environment.getExternalStorageState()
        PreyLogger.d("Deleting folder: $accessable from SD")
        if (Environment.MEDIA_MOUNTED == accessable) {
            PreyLogger.d("accessable")
            val dir = File(Environment.getExternalStorageDirectory().toString() + "")
            deleteRecursive(dir)
        }
    }

    /**
     * Recursively deletes a file or directory and all its contents.
     *
     * @param fileOrDirectory the file or directory to delete
     */
    fun deleteRecursive(fileOrDirectory: File) {
        PreyLogger.d("name:" + fileOrDirectory.name)
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }
}