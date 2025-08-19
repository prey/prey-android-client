/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.http

import java.io.InputStream

/**
 * Represents a file entity with various properties.
 */
class EntityFile {
    private var fileId: String? = null // Unique identifier for the file
    private var fileType: String? = null // Type of the file
    private var name: String? = null // Name of the file
    private var fileName: String? = null // FileName of the file
    private var fileMimeType: String? = null // MIME type of the file
    private var fileInputStream: InputStream? = null // Input stream of the file
    private var fileSize: Int = 0 // Size of the file in bytes

    /**
     * Gets the file ID.
     *
     * @return The file ID.
     */
    fun getFileId(): String? {
        return fileId
    }

    /**
     * Sets the file ID.
     *
     * @param id The file ID to set.
     */
    fun setFileId(id: String?) {
        fileId = id
    }

    /**
     * Gets the file type.
     *
     * @return The file type.
     */
    fun getFileType(): String? {
        return fileType
    }

    /**
     * Sets the file type.
     *
     * @param type The file type to set.
     */
    fun setFileType(type: String?) {
        fileType = type
    }

    /**
     * Gets the name.
     *
     * @return The file name.
     */
    fun getName(): String? {
        return name
    }

    /**
     * Sets the name.
     *
     * @param name The file name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * Gets the file name.
     *
     * @return The file name.
     */
    fun getFileName(): String? {
        return fileName
    }

    /**
     * Sets the file name.
     *
     * @param name The file name to set.
     */
    fun setFileName(name: String?) {
        fileName = name
    }

    /**
     * Gets the file MIME type.
     *
     * @return The file MIME type.
     */
    fun getFileMimeType(): String? {
        return fileMimeType
    }

    /**
     * Sets the file MIME type.
     *
     * @param mimeType The file MIME type to set.
     */
    fun setFileMimeType(mimeType: String?) {
        fileMimeType = mimeType
    }

    /**
     * Gets the file input stream.
     *
     * @return The file input stream.
     */
    fun getFileInputStream(): InputStream? {
        return fileInputStream
    }

    /**
     * Sets the file input stream.
     *
     * @param stream The file input stream to set.
     */
    fun setFileInputStream(stream: InputStream?) {
        fileInputStream = stream
    }

    /**
     * Gets the file size in bytes.
     *
     * @return The file size in bytes.
     */
    fun getFileSize(): Int {
        return fileSize
    }

    /**
     * Sets the file size in bytes.
     *
     * @param size The file size in bytes to set.
     */
    fun setFileSize(size: Int) {
        fileSize = size
    }

}