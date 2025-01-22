/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval.kotlin

class FileretrievalDto {
    private var fileId: String? = null
    private var path: String? = null
    private var size: Long = 0
    private var status: Int = 0
    private var total: Long = 0
    private var name: String? = null

    fun getFileId(): String {
        return fileId!!
    }

    fun setFileId(fileId: String?) {
        this.fileId = fileId
    }

    fun getPath(): String {
        return path!!
    }

    fun setPath(path: String?) {
        this.path = path
    }

    fun getSize(): Long {
        return size
    }

    fun setSize(size: Long) {
        this.size = size
    }

    fun getStatus(): Int {
        return status
    }

    fun setStatus(status: Int) {
        this.status = status
    }

    fun getTotal(): Long {
        return total
    }

    fun setTotal(total: Long) {
        this.total = total
    }

    fun getName(): String {
        return name!!
    }

    fun setName(name: String?) {
        this.name = name
    }
}
