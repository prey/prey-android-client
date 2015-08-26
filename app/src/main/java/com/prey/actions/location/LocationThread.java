package com.prey.actions.location;

/**
 * Created by oso on 24-08-15.
 */

import java.util.Map;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;

import android.content.Context;
import android.telephony.SmsManager;

public class LocationThread extends Thread {

    private Context ctx;
    private String phoneNumber;

    public LocationThread(Context ctx, String phoneNumber) {
        this.ctx = ctx;
        this.phoneNumber = phoneNumber;
    }

    public void run() {
        PreyLogger.i("run location thread");
        HttpDataService data = LocationUtil.dataLocation(ctx);
        Map<String, String> parametersMap = data.getDataList();
        if (phoneNumber != null && !"".equals(phoneNumber)) {
            String lat = parametersMap.get("lat");
            String lng = parametersMap.get("lng");
            String message = "location http://maps.google.com/?q=" + lat + "," + lng;
            sendSMS(phoneNumber, message);
        }
        PreyLogger.i("lat:" + parametersMap.get("lat") + " lng:" + parametersMap.get("lng"));
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

}
