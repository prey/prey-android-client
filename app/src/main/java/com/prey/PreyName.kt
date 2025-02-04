/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

class PreyName {
    private var code: Int = 0
    private var name: String? = null
    private var error: String? = null

    fun getCode(): Int {
        return code
    }

    fun setCode(code: Int) {
        this.code = code
    }

    fun getName(): String {
        return name!!
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getError(): String {
        return error!!
    }

    fun setError(error: String?) {
        this.error = error
    }
}