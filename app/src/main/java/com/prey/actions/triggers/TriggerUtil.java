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

import org.json.JSONObject;

public class TriggerUtil {

    static SimpleDateFormat sdf_dh = new SimpleDateFormat("yyyyMMddHHmmss");
    static SimpleDateFormat sdf_h = new SimpleDateFormat("HHmmss");
    static SimpleDateFormat sdf_d = new SimpleDateFormat("yyyyMMdd");

    public static boolean haveRange(List<TriggerEventDto> listEvents) {
        for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
            TriggerEventDto event = listEvents.get(j);
            if (TimeTrigger.RANGE_TIME.equals(event.type)) {
                return true;
            }
            if (TimeTrigger.REPEAT_RANGE_TIME.equals(event.type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validRange(List<TriggerEventDto> listEvents) {
        for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
            TriggerEventDto event = listEvents.get(j);
            if (TimeTrigger.RANGE_TIME.equals(event.type)) {
                return validRange(event);
            }
            if (TimeTrigger.REPEAT_RANGE_TIME.equals(event.type)) {
                return validRangeTime(event);
            }
        }
        return false;
    }

    public static boolean validRangeTime(TriggerEventDto event) {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int day_od_week = cal.get(Calendar.DAY_OF_WEEK);
        PreyLogger.d("day_od_week:" + day_od_week);
        try {
            JSONObject root = new JSONObject(event.info);
            String daysOfWeek = root.getString("days_of_week");
            daysOfWeek=daysOfWeek.replace("[","");
            daysOfWeek=daysOfWeek.replace("]","");
            String[] parts = daysOfWeek.split(",");
            String hour_fromSt = root.getString("hour_from");
            String hour_untilSt = root.getString("hour_until");
            int hour_from = Integer.parseInt(hour_fromSt);
            int hour_until = Integer.parseInt(hour_untilSt);
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
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            boolean isDay = false;
            for (int i = 0; parts != null && i < parts.length; i++) {
                String daySt = parts[i];
                int day=TriggerUtil.dayTrigger(daySt);
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
            PreyLogger.e("error validRangeTime:"+e.getMessage(),e);
        }
        return false;
    }

    public static boolean validRange(TriggerEventDto event) {
        try {
            Date nowDate = new Date();
            String nowSt = sdf_d.format(nowDate);
            double now = Double.parseDouble(nowSt);
            JSONObject root = new JSONObject(event.info);
            double from = root.getDouble("from");
            double until = root.getDouble("until");
            boolean a = false;
            if (from <= now) {
                a = true;
            }
            boolean b = false;
            if (until >= now) {
                b = true;
            }
            if (a && b) {
                return true;
            }
        } catch (Exception e) {
            PreyLogger.e("error validRange:"+e.getMessage(),e);
        }
        return false;
    }

    public static boolean validateTrigger(TriggerDto trigger) {
        Date now = new Date();
        List<TriggerEventDto> listEvents = TriggerParse.TriggerEvents(trigger.getEvents());
        for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
            TriggerEventDto event = listEvents.get(j);
            if (TimeTrigger.EXACT_TIME.equals(event.type)) {
                boolean valid=true;
                try {
                    JSONObject json = new JSONObject(event.info);
                    String dateTime = null;
                    dateTime = json.getString("date");
                    PreyLogger.d(String.format("TimeTrigger dateTime:%s", dateTime));
                    Date date = TimeTrigger.EXACT_TIME_FORMAT_SDF.parse(dateTime);
                    //increased to 15
                    valid = validDateAroundMinutes(date, 15);
                } catch (Exception e) {
                    PreyLogger.e(String.format("Error :%s", e.getMessage()), e);
                }
                return valid;
            }
            if (TimeTrigger.REPEAT_TIME.equals(event.type)) {
                try {
                    JSONObject json = new JSONObject(event.info);
                    String daysOfWeek = json.getString("days_of_week");
                    daysOfWeek = daysOfWeek.replace("[", "");
                    daysOfWeek = daysOfWeek.replace("]", "");
                    String[] parts = daysOfWeek.split(",");
                    Calendar cal=Calendar.getInstance();
                    cal.setTime(now);
                    int dayNow=cal.get(Calendar.DAY_OF_WEEK);
                    String hourSt = json.getString("hour");
                    String minuteSt = json.getString("minute");
                    String secondSt = json.getString("second");
                    int hour =0;
                    try{hour=Integer.parseInt(hourSt);}catch (Exception e){PreyLogger.e("Error:"+e.getMessage(),e);}
                    int minute =0;
                    try{minute=Integer.parseInt(minuteSt);}catch (Exception e){PreyLogger.e("Error:"+e.getMessage(),e);}
                    int second =0;
                    try{second=Integer.parseInt(secondSt);}catch (Exception e){PreyLogger.e("Error:"+e.getMessage(),e);}
                    Calendar calender = Calendar.getInstance();
                    calender.set(Calendar.HOUR_OF_DAY, hour);
                    calender.set(Calendar.MINUTE, minute);
                    calender.set(Calendar.SECOND, 0);
                    calender.set(Calendar.MILLISECOND, 0);
                    Date dateTime=calender.getTime();
                    PreyLogger.d("TimeTrigger dateTime:" + dateTime);
                    boolean valid=false;
                    for (int i = 0; parts != null && i < parts.length; i++) {
                        String daySt = parts[i];
                        int day = dayTrigger(daySt);
                        if(day==dayNow){
                            valid=true;
                            PreyLogger.d("TimeTrigger day==dayNow");
                        }
                    }
                    if (valid) {
                        //increased to 15
                        valid = validDateAroundMinutes(dateTime, 15);
                        PreyLogger.d("TimeTrigger validDateAroundMinutes");
                    }
                    return valid;
                } catch (Exception e) {
                    PreyLogger.e("Error:"+e.getMessage(),e);
                }
            }
        }
        return true;
    }

    public static int dayTrigger(String daySt) {
        int day=-1;
        switch(daySt)
        {
            case "0":
                day = Calendar.SUNDAY;
                break;
            case "1":
                day = Calendar.MONDAY;
                break;
            case "2":
                day = Calendar.TUESDAY;
                break;
            case "3":
                day = Calendar.WEDNESDAY;
                break;
            case "4":
                day = Calendar.THURSDAY;
                break;
            case "5":
                day = Calendar.FRIDAY;
                break;
            default:
                day = Calendar.SATURDAY;
        }
        return day;
    }


    public static boolean validDateAroundMinutes(Date date,int minutes) {
        Date now = new Date();
        boolean valid=true;
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, -2);
        Date lessMinutes = cal.getTime();
        long datetime=date.getTime();
        if (datetime < lessMinutes.getTime()) {
            PreyLogger.d("less minutes");
            valid=false;
        }
        cal.setTime(now);
        cal.add(Calendar.MINUTE, minutes);
        Date moreMinutes = cal.getTime();
        if (datetime > moreMinutes.getTime()) {
            PreyLogger.d("more minutes");
            valid=false;
        }
        return valid;
    }

}