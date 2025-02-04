/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.http

import java.io.InputStream

class EntityFile {
    private var idFile: String? = null
    private var type: String? = null
    private var name: String? = null
    private var mimeType: String? = null
    private var file: InputStream? = null
    private var length: Int = 0
    private var fileName: String? = null

    /**
     * Method return the file name
     * @return file name
     */
    fun getFilename(): String? {
        return fileName
    }

    /**
     * Method update the file name
     * @param fileName
     */
    fun setFilename(fileName: String) {
        this.fileName = fileName
    }

    fun getLength(): Int {
        return length
    }

    fun setLength(length: Int) {
        this.length = length
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getMimeType(): String? {
        return mimeType
    }

    fun setMimeType(mimeType: String?) {
        this.mimeType = mimeType
    }

    fun getFile(): InputStream? {
        return file
    }

    fun setFile(file: InputStream?) {
        this.file = file
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String?) {
        this.type = type
    }

    fun getIdFile(): String? {
        return idFile
    }

    fun setIdFile(idFile: String?) {
        this.idFile = idFile
    }
}