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

public class LocationLowBatteryRunner implements Runnable {

    private Context ctx = null;


    public LocationLowBatteryRunner(Context ctx) {
        this.ctx = ctx;
    }

    public void run() {
        PreyLogger.d("EVENT LocationLowBatteryRunner");
        try {
            String jsonString = "[ {\"command\": \"get\",\"target\": \"location_low_battery\",\"options\": {}}]";
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
            cal.add(Calendar.HOUR, -3);
            long leastSixHours = cal.getTimeInMillis();
            long locationLowBatteryDate = PreyConfig.getPreyConfig(ctx).getLocationLowBatteryDate();
            PreyLogger.d("EVENT locationLowBatteryDate :" + locationLowBatteryDate + " " + sdf.format(new Date(locationLowBatteryDate)));
            PreyLogger.d("EVENT leastSixHours   :" + leastSixHours + " " + sdf.format(new Date(leastSixHours)));
            PreyLogger.d("EVENT diff:"+(leastSixHours-locationLowBatteryDate));
            if (locationLowBatteryDate == 0 || leastSixHours > locationLowBatteryDate) {
                long now = new Date().getTime();
                PreyConfig.getPreyConfig(ctx).setLocationLowBatteryDate(now);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
