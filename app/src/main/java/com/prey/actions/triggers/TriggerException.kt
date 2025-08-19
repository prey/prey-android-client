/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

/**
 * Custom exception class for trigger-related errors.
 *
 * This class extends the built-in Exception class and provides additional constructors
 * for creating TriggerException instances with custom error messages and/or underlying throwables.
 */
class TriggerException : Exception {

    var code: Int = -1

    constructor(detailMessage: String?, throwable: Throwable?) : super(detailMessage, throwable)

    constructor(message: String?) : super(message)

    constructor(throwable: Throwable?) : super(throwable)

    constructor(code: Int, message: String?) : super(message) {
        this.code = code
    }

    companion object {
        private const val serialVersionUID = 1L
    }

}