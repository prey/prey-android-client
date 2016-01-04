/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionsController;
import com.prey.events.Event;
import com.prey.json.parser.JSONParser;

public class SignalFlareRunner implements Runnable {

    private Context ctx = null;


    public SignalFlareRunner(Context ctx) {
        this.ctx = ctx;
    }

    public void run() {
        PreyLogger.d("SignalFlareRunner");
        PreyLogger.d("SignalFlareRunner");
        PreyLogger.d("SignalFlareRunner");
        try {
            String jsonString = "[ {\"command\": \"get\",\"target\": \"signalflare\",\"options\": {}}]";
            List<JSONObject> jsonObjectList = new JSONParser().getJSONFromTxt(ctx, jsonString.toString());
            if (jsonObjectList != null && jsonObjectList.size() > 0) {
                ActionsController.getInstance(ctx).runActionJson(ctx, jsonObjectList);
            }
        } catch (Exception e) {
        }
    }

    private static  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault());

    public static boolean isValid(Context ctx) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.HOUR, -24);
            long leastSixHours = cal.getTimeInMillis();
            long signalFlareDate = PreyConfig.getPreyConfig(ctx).getSignalFlareDate();
            PreyLogger.d("signalFlareDate :" + signalFlareDate + " " + sdf.format(new Date(signalFlareDate)));
            PreyLogger.d("leastSixHours   :" + leastSixHours + " " + sdf.format(new Date(leastSixHours)));
            PreyLogger.d("diff:"+(leastSixHours-signalFlareDate));
            if (signalFlareDate == 0 || leastSixHours > signalFlareDate) {
                long now = new Date().getTime();
                PreyConfig.getPreyConfig(ctx).setSignalFlareDate(now);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
