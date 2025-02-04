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

object WipeUtil {
    fun deleteSD() {
        val accessable = Environment.getExternalStorageState()
        PreyLogger.d("Deleting folder: $accessable from SD")
        if (Environment.MEDIA_MOUNTED == accessable) {
            PreyLogger.d("accessable")
            val dir = File(Environment.getExternalStorageDirectory().toString() + "")
            deleteRecursive(dir)
        }
    }

    fun deleteRecursive(fileOrDirectory: File) {
        PreyLogger.d("name:" + fileOrDirectory.name)
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }
}