/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.exceptions.kotlin

class PreyException : Exception {
    /**
     * @param detailMessage
     * @param throwable
     */
    constructor(detailMessage: String?, throwable: Throwable?) : super(detailMessage, throwable)

    constructor(message: String?) : super(message)

    constructor(throwable: Throwable?) : super(throwable)

    companion object {
        private const val serialVersionUID = 1L
    }
}
