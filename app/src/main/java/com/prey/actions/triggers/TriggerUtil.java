/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import com.prey.PreyLogger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class TriggerUtil {

    public static boolean haveRange(List<TriggerEventDto> listEvents) {
        for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
            TriggerEventDto event = listEvents.get(j);
            if ("range_time".equals(event.type)) {
                return true;
            }
            if ("repeat_range_time".equals(event.type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validRange(List<TriggerEventDto> listEvents) {
        for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
            TriggerEventDto event = listEvents.get(j);
            if ("range_time".equals(event.type)) {
                return validRange(event);
            }
            if ("repeat_range_time".equals(event.type)) {
                return validRangeTime(event);
            }
        }
        return false;
    }

    static SimpleDateFormat sdf_dh = new SimpleDateFormat("yyyyMMddHHmmss");
    static SimpleDateFormat sdf_h = new SimpleDateFormat("HHmmss");
    static SimpleDateFormat sdf_d = new SimpleDateFormat("yyyyMMdd");

    public static boolean validRangeTime(TriggerEventDto event) {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int day_od_week = cal.get(Calendar.DAY_OF_WEEK);
        PreyLogger.d("day_od_week:" + day_od_week);
        try {
            JSONObject root = new JSONObject(event.info);
            JSONArray array = root.getJSONArray("days_of_week");
            int hour_from = root.getInt("hour_from");
            int hour_until = root.getInt("hour_until");
            int until = -1;
            try {
                until = root.getInt("until");
                PreyLogger.d("until:" + until);
                String fechaSt = sdf_d.format(now);
                int fecha = Integer.parseInt(fechaSt);
                if (fecha > until) {
                    PreyLogger.d("fecha>until");
                    return false;
                } else {
                    PreyLogger.d("fecha<=until");
                }
            } catch (Exception e) {
            }
            boolean isDay = false;
            for (int j = 0; array != null && j < array.length(); j++) {
                int day = array.getInt(j);
                //PreyLogger.d("day:" + day);
                if (day == day_od_week)
                    isDay = true;
            }
            PreyLogger.d("isDay:" + isDay);
            String horaSt = sdf_h.format(now);
            int hora = Integer.parseInt(horaSt);
            PreyLogger.d("horaSt:" + horaSt);
            if (isDay) {
                boolean a = false;
                if (hour_from <= hora) {
                    PreyLogger.d("a:");
                    a = true;

                }
                boolean b = false;
                if (hour_until >= hora) {
                    PreyLogger.d("b:");
                    b = true;
                }
                if (a && b) {

                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean validRange(TriggerEventDto event) {

        try {
            Date now = new Date();
            String fecha = sdf_dh.format(now);

            double fechaDouble = Double.parseDouble(fecha);

            JSONObject root = new JSONObject(event.info);
            double from = root.getDouble("from");
            double until = root.getDouble("until");

            boolean a = false;
            if (from <= fechaDouble) {

                a = true;

            }
            boolean b = false;
            if (until >= fechaDouble) {

                b = true;
            }
            if (a && b) {

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
