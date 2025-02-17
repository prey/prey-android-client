/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval

import android.content.Context
import com.prey.PreyLogger

/**
 * Data source for file retrieval operations.
 *
 * This class provides a interface to interact with the file retrieval database.
 * It encapsulates the database operations and provides a simple API for creating,
 * deleting, and retrieving file retrieval data.
 *
 * @param context The application context.
 */
class FileretrievalDatasource(context: Context?) {
    private val dbHelper = FileretrievalOpenHelper(context)

    /**
     * Creates a new file retrieval entry in the database.
     *
     * If the entry already exists, it will be updated instead.
     *
     * @param dto The file retrieval data to be inserted or updated.
     */
    fun createFileretrieval(dto: FileretrievalDto) {
        try {
            dbHelper.insertFileretrieval(dto)
        } catch (e: Exception) {
            try {
                dbHelper.updateFileretrieval(dto)
            } catch (e1: Exception) {
                PreyLogger.e("error db update:${e1.message}", e1)
            }
        }
    }

    /**
     * Deletes a file retrieval entry from the database.
     *
     * @param id The ID of the file retrieval entry to be deleted.
     */
    fun deleteFileretrieval(id: String) {
        dbHelper.deleteFileretrieval(id)
    }

    /**
     * Retrieves all file retrieval entries from the database.
     *
     * @return A list of file retrieval data.
     */
    fun allFileretrieval(): List<FileretrievalDto> {
        return dbHelper.allFileretrieval
    }

    /**
     * Retrieves a file retrieval entry from the database by ID.
     *
     * @param id The ID of the file retrieval entry to be retrieved.
     * @return The file retrieval data, or null if not found.
     */
    fun getFileretrievals(id: String): FileretrievalDto? {
        return dbHelper.getFileretrieval(id)
    }

    /**
     * Deletes all file retrieval entries from the database.
     */
    fun deleteAllFileretrieval() {
        dbHelper.deleteAllFileretrieval()
    }
}
