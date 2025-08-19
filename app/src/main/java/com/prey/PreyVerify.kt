/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

/**
 * Represents a Prey verification result, containing a status code and description.
 */
class PreyVerify {

    private var statusCode: Int = -1
    private var statusDescription: String? = null
    fun getStatusCode(): Int {
        return statusCode
    }

    fun setStatusCode(statusCode: Int) {
        this.statusCode = statusCode
    }

    fun getStatusDescription(): String {
        return statusDescription!!
    }

    fun setStatusDescription(statusDescription: String?) {
        this.statusDescription = statusDescription
    }

}