/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.sms;

import android.content.Context;

public class SmsThread extends Thread {
    private Context ctx;

    private String messageSMS;
    private String phoneNumber;

    public SmsThread(Context ctx, String messageSMS, String phoneNumber) {
        this.messageSMS = messageSMS;
        this.ctx = ctx;
        this.phoneNumber = phoneNumber;
    }

    public void run() {
        SMSFactory.execute(ctx, messageSMS, phoneNumber);
    }
}