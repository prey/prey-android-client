/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.wipe

import android.content.Context
import android.os.Environment

import com.prey.PreyLogger
import com.prey.backwardcompatibility.FroyoSupport

import java.io.File

/**
 * Utility class for wiping data from the device's external storage.
 */
class PreyWipe : WipeInterface {

    /**
     * Deletes all data from the device's external storage.
     */
    override fun deleteSD(context: Context) {
        val externalStorageState = Environment.getExternalStorageState()
        PreyLogger.d("Deleting folder: $externalStorageState from SD")
        if (Environment.MEDIA_MOUNTED == externalStorageState) {
            val dir = File(Environment.getExternalStorageDirectory().toString())
            deleteRecursive(dir)
        }
    }

    override fun wipeData(context: Context) {
        FroyoSupport.getInstance(context).wipeData()
    }

    /**
     * Recursively deletes a file or directory and all its contents.
     *
     * @param fileOrDirectory the file or directory to delete
     */
    private fun deleteRecursive(fileOrDirectory: File) {
        PreyLogger.d("deleteRecursive name:${fileOrDirectory.name}")
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()!!) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }

    companion object {
        private var instance: PreyWipe? = null

        fun getInstance(): PreyWipe {
            if (instance == null) {
                instance = PreyWipe()
            }
            return instance!!
        }
    }

}