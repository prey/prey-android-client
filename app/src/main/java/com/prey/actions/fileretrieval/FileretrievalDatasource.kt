/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval

import android.content.Context
import com.prey.PreyLogger

class FileretrievalDatasource(context: Context?) {
    private val dbHelper = FileretrievalOpenHelper(context)

    fun createFileretrieval(dto: FileretrievalDto) {
        try {
            dbHelper.insertFileretrieval(dto)
        } catch (e: Exception) {
            try {
                dbHelper.updateFileretrieval(dto)
            } catch (e1: Exception) {
                PreyLogger.e("error db update:" + e1.message, e1)
            }
        }
    }

    fun deleteFileretrieval(id: String) {
        dbHelper.deleteFileretrieval(id)
    }

    fun allFileretrieval(): List<FileretrievalDto> {
        return dbHelper.allFileretrieval
    }

    fun getFileretrievals(id: String): FileretrievalDto? {
        return dbHelper.getFileretrieval(id)
    }

    fun deleteAllFileretrieval() {
        dbHelper.deleteAllFileretrieval()
    }
}
