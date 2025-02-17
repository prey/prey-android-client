/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval

/**
 * Data Transfer Object (DTO) for file retrieval.
 * This class represents a file being retrieved and its associated metadata.
 */
class FileretrievalDto {
    /**
     * Unique identifier for the file.
     */
    private var fileId: String? = null
    /**
     * Path to the file on the device.
     */
    private var path: String? = null
    /**
     * Size of the file in bytes.
     */
    private var size: Long = 0
    /**
     * Status of the file retrieval process.
     * 0 - Not started
     * 1 - In progress
     * 2 - Completed
     */
    private var status: Int = 0
    /**
     * Total size of the file to be retrieved.
     */
    private var total: Long = 0
    /**
     * Name of the file.
     */
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
