/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.sms;


import android.content.Context;

import com.prey.PreyConfig;
import com.prey.actions.observer.ActionsController;

import org.json.JSONObject;

import java.util.List;

public class SMSFactory {

    public static void execute(Context ctx, String command, String phoneNumber) {
        String secretKey = SMSUtil.getSecretKey(command);
        boolean isPasswordOk = false;
        try {
            isPasswordOk = PreyConfig.getPreyConfig(ctx).getPinNumber().equals(secretKey);
        } catch (Exception e) {
        }
        if (isPasswordOk) {
            List<JSONObject> jsonList = SMSParser.getJSONListFromText(command, phoneNumber);
            ActionsController.getInstance(ctx).runActionJson(ctx, jsonList);
        }
    }

}