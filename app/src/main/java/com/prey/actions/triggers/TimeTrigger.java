/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimeTrigger {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final int ADD_TRIGGER_ID = 10000;
    public static final int MUL_TRIGGER_DAY = 10000;

    public static void updateTrigger(Context ctx, TriggerDto trigger) {
        String triggerName = trigger.getName();
        PreyLogger.d("TimeTrigger triggerName:" + triggerName + " id:" + trigger.id);
        List<TriggerEventDto> listEvents = TriggerParse.TriggerEvents(trigger.getEvents());
        Date now = new Date();
        PreyLogger.d("TimeTrigger now:" + sdf.format(now));
        for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
            TriggerEventDto event = listEvents.get(j);
            PreyLogger.d("TimeTrigger event.type:" + event.type + " ");
            if ("exact_time".equals(event.type)) {
                try {
                    JSONObject json = new JSONObject(event.info);
                    String dateTime = null;
                    dateTime = json.getString("date");
                    PreyLogger.d("TimeTrigger dateTime:" + dateTime);
                    Date date = sdf.parse(dateTime);
                    PreyLogger.d("TimeTrigger format:" + sdf.format(date));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(date.getTime());
                    PreyLogger.d("TimeTrigger format:" + sdf.format(calendar.getTime()));
                    Intent intent = new Intent(ctx, TimeTriggerReceiver.class);
                    intent.putExtra("trigger_id", "" + trigger.id);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, (trigger.id + ADD_TRIGGER_ID), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                        PreyLogger.d("TimeTrigger----------set");
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                            PreyLogger.d("TimeTrigger----------setExact");
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } else {
                            PreyLogger.d("TimeTrigger----------setExactAndAllowWhileIdle");
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                    }
                } catch (Exception e) {
                    PreyLogger.e("error:" + e.getMessage(), e);
                }
            }
            if ("repeat_time".equals(event.type)) {
                try {
                    JSONObject json = new JSONObject(event.info);


                    int hour = json.getInt("hour");
                    int minute = json.getInt("minute");
                    int second = json.getInt("second");
                    Intent myIntent = new Intent(ctx, TimeTriggerReceiver.class);
                    myIntent.putExtra("trigger_id", "" + trigger.id);

                    JSONArray array = json.getJSONArray("days_of_week");


                    PreyLogger.d("TimeTrigger hour:" + hour + " minute:" + minute + " second:" + second);

                    for (int i = 0; array != null && i < array.length(); i++) {
                        int day = array.getInt(i);
                        // calendar.setTimeInMillis(System.currentTimeMillis());
                        Calendar calender = Calendar.getInstance();

                        PreyLogger.d("TimeTrigger Calendar.DAY_OF_WEEK:" + calender.get(Calendar.DAY_OF_WEEK));

                        calender.set(Calendar.HOUR_OF_DAY, hour);  //pass hour which you have select
                        calender.set(Calendar.MINUTE, minute);  //pass min which you have select
                        calender.set(Calendar.SECOND, 0);
                        calender.set(Calendar.MILLISECOND, 0);


                        PreyLogger.d("TimeTrigger array DAY_OF_WEEK:" + day);

                        calender.set(Calendar.DAY_OF_WEEK, day);
                        Intent intent = new Intent(ctx, TimeTriggerReceiver.class);
                        intent.putExtra("trigger_id", "" + trigger.id);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, (trigger.id + ADD_TRIGGER_ID) + (day * MUL_TRIGGER_DAY), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);


                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);


                    }
                } catch (Exception e) {
                    PreyLogger.e("error:" + e.getMessage(), e);
                }
            }

        }
    }
}