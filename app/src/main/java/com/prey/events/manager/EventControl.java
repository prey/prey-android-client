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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.prey.PreyLogger;

public class EventControl {

    private static EventControl instance = null;
    private static Map<String, Long> map = null;
    private SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault());

    private EventControl() {
        map = new HashMap<String, Long>();
    }

    public static EventControl getInstance() {
        if (instance == null) {
            instance = new EventControl();
        }
        return instance;
    }

    public boolean valida(JSONObject json) {
        String state = "";
        double percentage = -1;
        try {
            JSONObject jsonBattery = json.getJSONObject("battery_status");
            state = jsonBattery.getString("state");
            String remaining = jsonBattery.getString("percentage_remaining");
            PreyLogger.d("state:" + state + " remaining:" + remaining);
            percentage = Double.parseDouble(remaining);
        } catch (Exception e) {
            percentage = -1;
        }
        Date nowDate = new Date();
        long now = nowDate.getTime();
        if ("discharging".equals(state) || "stopped_charging".equals(state)) {
            if (percentage > 0) {
                if (map.containsKey(state)) {
                    long time = map.get(state);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(time);
                    if (percentage <= 15) {
                        cal.add(Calendar.MINUTE, 4);
                    } else {
                        cal.add(Calendar.MINUTE, 1);
                    }
                    long timeMore = cal.getTimeInMillis();
                    PreyLogger.d("now        :" + now + " " + sdf2.format(new Date(now)));
                    PreyLogger.d("timeMore:" + timeMore + " " + sdf2.format(new Date(timeMore)));
                    if (timeMore > now) {
                        return false;
                    } else {
                        map.put(state, now);
                        return true;
                    }
                } else {
                    map.put(state, now);
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

}

