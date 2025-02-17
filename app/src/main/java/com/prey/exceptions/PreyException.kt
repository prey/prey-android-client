/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.exceptions

/**
 * Custom exception class for Prey-related errors.
 *
 * This class extends the built-in Exception class and provides additional constructors
 * for creating PreyException instances with custom error messages and/or underlying throwables.
 */
class PreyException : Exception {

    /**
     * Constructs a new PreyException instance with the specified detail message and underlying throwable.
     *
     * @param detailMessage the detailed error message
     * @param throwable the underlying throwable that caused this exception
     */
    constructor(detailMessage: String?, throwable: Throwable?) : super(detailMessage, throwable)

    /**
     * Constructs a new PreyException instance with the specified error message.
     *
     * @param message the error message
     */
    constructor(message: String?) : super(message)

    /**
     * Constructs a new PreyException instance with the specified underlying throwable.
     *
     * @param throwable the underlying throwable that caused this exception
     */
    constructor(throwable: Throwable?) : super(throwable)

    companion object {
        private const val serialVersionUID = 1L
    }
}
