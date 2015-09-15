/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.exceptions;

public class PreyException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param detailMessage
     * @param throwable
     */
    public PreyException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PreyException(String message) {
        super(message);
    }

}
