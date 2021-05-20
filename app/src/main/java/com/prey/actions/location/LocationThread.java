/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import java.util.Map;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.core.app.ActivityCompat;

public class LocationThread extends Thread {

    private Context ctx;
    private String phoneNumber;

    public LocationThread(Context ctx, String phoneNumber) {
        this.ctx = ctx;
        this.phoneNumber = phoneNumber;
    }

    public void run() {
        PreyLogger.d("run location thread");
        HttpDataService data = LocationUtil.dataLocation(ctx,null,false);
        if(data!=null){
            Map<String, String> parametersMap = data.getDataList();
            if (phoneNumber != null && !"".equals(phoneNumber)) {
                String lat = parametersMap.get("lat");
                String lng = parametersMap.get("lng");
                String message = "location http://maps.google.com/?q=" + lat + "," + lng;
                sendSMS(phoneNumber, message);
            }
            PreyLogger.d("lat:" + parametersMap.get("lat") + " lng:" + parametersMap.get("lng"));
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED )) {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }

}