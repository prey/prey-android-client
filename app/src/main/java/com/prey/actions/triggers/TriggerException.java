/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

public class TriggerException  extends Exception {

    private static final long serialVersionUID = 1L;

    public int code=-1;

    public TriggerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TriggerException(String message) {
        super(message);
    }

    public TriggerException(Throwable throwable) {
        super(throwable);
    }

    public TriggerException(int code,String message) {
        super(message);
        this.code=code;
    }

}
