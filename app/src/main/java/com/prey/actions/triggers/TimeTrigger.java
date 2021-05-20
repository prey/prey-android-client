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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimeTrigger {

    public static SimpleDateFormat EXACT_TIME_FORMAT_SDF = new SimpleDateFormat("yyyyMMddHHmmss");
    public static SimpleDateFormat REPEAT_TIME_FORMAT_SDF = new SimpleDateFormat("yyyyMMdd");
    public static SimpleDateFormat REPEAT_RANGE_TIME_FORMAT_SDF = new SimpleDateFormat("HHmmss");

    public static final String REPEAT_TIME="repeat_time";
    public static final String EXACT_TIME="exact_time";
    public static final String RANGE_TIME="range_time";
    public static final String REPEAT_RANGE_TIME="repeat_range_time";

    public static final int ADD_TRIGGER_ID = 1000;

    public static void updateTrigger(Context ctx, TriggerDto trigger) throws TriggerException{
        String triggerName = trigger.getName();
        PreyLogger.d("TimeTrigger triggerName:" + triggerName + " id:" + trigger.id);
        List<TriggerEventDto> listEvents = TriggerParse.TriggerEvents(trigger.getEvents());
        Date now = new Date();
        PreyLogger.d("TimeTrigger now:" + EXACT_TIME_FORMAT_SDF.format(now));
        for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
            TriggerEventDto event = listEvents.get(j);
            PreyLogger.d("TimeTrigger event.type:" + event.type + " ");
            if(REPEAT_RANGE_TIME.equals(event.type)){
                try {
                    JSONObject json = new JSONObject(event.info);
                    String hour_from ="";
                    try {
                        hour_from = json.getString("hour_from");
                    }catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    String hour_until ="";
                    try {
                        hour_until = json.getString("hour_until");
                    }catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    Date hourFrom=null;
                    Date hourUntil=null;
                    if(!"".equals(hour_from)){
                        try {
                            hourFrom = REPEAT_RANGE_TIME_FORMAT_SDF.parse(hour_from);
                        }catch (Exception e){
                            throw new TriggerException(2,"Invalid trigger format");
                        }
                    }
                    if(!"".equals(hour_until)){
                        try {
                            hourUntil = REPEAT_RANGE_TIME_FORMAT_SDF.parse(hour_until);
                        }catch (Exception e){
                            throw new TriggerException(2,"Invalid trigger format");
                        }
                    }
                    String hourFromSt=REPEAT_RANGE_TIME_FORMAT_SDF.format(hourFrom);
                    String hourUntilSt=REPEAT_RANGE_TIME_FORMAT_SDF.format(hourUntil);
                    int hourFromInt=Integer.parseInt(hourFromSt);
                    int hourUntilInt=Integer.parseInt(hourUntilSt);
                    if(hourFromInt>hourUntilInt) {
                        throw new TriggerException(4,"The execution range dates doesn't make sense");
                    }
                    Date dateUntil=null;
                    String until ="";
                    try {
                        until = json.getString("until");
                    }catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    if(!"".equals(until)){
                        try {
                            dateUntil = REPEAT_TIME_FORMAT_SDF.parse(until);
                        }catch (Exception e){
                            throw new TriggerException(2,"Invalid trigger format");
                        }
                    }
                    String dateUntilSt= REPEAT_TIME_FORMAT_SDF.format(dateUntil);
                    String dateNowSt= REPEAT_TIME_FORMAT_SDF.format(now);

                    int intUntil=Integer.parseInt(dateUntilSt);
                    int intNow=Integer.parseInt(dateNowSt);
                    if(dateUntil!=null&&intNow>intUntil) {
                        throw new TriggerException(4,"The execution range dates doesn't make sense");
                    }
                } catch (TriggerException te) {
                    throw te;
                } catch (Exception e) {
                    PreyLogger.e("error:" + e.getMessage(), e);
                    throw new TriggerException(0,"Unknown error:"+e.getMessage());
                }
            }
            if (RANGE_TIME.equals(event.type)) {
                try {
                    JSONObject json = new JSONObject(event.info);
                    String from ="";
                    try {
                        from = json.getString("from");
                    }catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    String until ="";
                    try {
                        until = json.getString("until");
                    }catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    Date dateFrom=null;
                    Date dateUntil=null;
                    if(!"".equals(from)){
                        try {
                            dateFrom = REPEAT_TIME_FORMAT_SDF.parse(from);
                        }catch (Exception e){
                            throw new TriggerException(2,"Invalid trigger format");
                        }
                    }
                    if(!"".equals(until)){
                        try {
                            dateUntil = REPEAT_TIME_FORMAT_SDF.parse(until);
                        }catch (Exception e){
                            throw new TriggerException(2,"Invalid trigger format");
                        }
                    }
                    if(dateFrom!=null&&dateUntil!=null){
                        String dateUntilSt= REPEAT_TIME_FORMAT_SDF.format(dateUntil);
                        String dateNowSt= REPEAT_TIME_FORMAT_SDF.format(now);
                        String dateFromSt= REPEAT_TIME_FORMAT_SDF.format(dateFrom);
                        int intUntil=Integer.parseInt(dateUntilSt);
                        int intNow=Integer.parseInt(dateNowSt);
                        int intFrom=Integer.parseInt(dateFromSt);
                        PreyLogger.d("Trigger TimeTrigger intUntil:"+intUntil);
                        PreyLogger.d("Trigger TimeTrigger intNow:"+intNow);
                        PreyLogger.d("Trigger TimeTrigger intFrom:"+intFrom);
                        if(intNow>intUntil) {
                            throw new TriggerException(4,"The execution range dates doesn't make sense");
                        }
                        if(intFrom>intUntil) {
                            throw new TriggerException(4,"The execution range dates doesn't make sense");
                        }
                    }else{
                        throw new TriggerException(0,"Unknown error from:"+from+" or until:"+until);
                    }
                } catch (TriggerException te) {
                    throw te;
                } catch (Exception e) {
                    PreyLogger.e("error:" + e.getMessage(), e);
                    throw new TriggerException(0,"Unknown error:"+e.getMessage());
                }
            }
            if (EXACT_TIME.equals(event.type)) {
                try {
                    JSONObject json = new JSONObject(event.info);
                    String dateTime = null;
                    dateTime = json.getString("date");
                    PreyLogger.d("TimeTrigger dateTime:" + dateTime);
                    Date date = EXACT_TIME_FORMAT_SDF.parse(dateTime);
                    if(now.getTime()<=date.getTime()) {
                        PreyLogger.d("TimeTrigger format:" + EXACT_TIME_FORMAT_SDF.format(date));
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(date.getTime());
                        PreyLogger.d("TimeTrigger format:" + EXACT_TIME_FORMAT_SDF.format(calendar.getTime()));
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
                    }else{
                        PreyLogger.d("Trigger TimeTrigger----------Date is less ");
                        throw new TriggerException(3,"Expired trigger");
                    }
                } catch (TriggerException te) {
                    throw te;
                } catch (Exception e) {
                    PreyLogger.e("error:" + e.getMessage(), e);
                    throw new TriggerException(0,"Unknown error:"+e.getMessage());
                }
            }
            if (REPEAT_TIME.equals(event.type)) {
                try {
                    JSONObject json = new JSONObject(event.info);
                    String until ="";
                    try {
                        until = json.getString("until");
                    }catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    if(!"".equals(until)){
                        Date dateUntil=null;
                        try {
                            dateUntil = REPEAT_TIME_FORMAT_SDF.parse(until);
                        }catch (Exception e){
                            throw new TriggerException(2,"Invalid trigger format");
                        }
                        String dateUntilSt= REPEAT_TIME_FORMAT_SDF.format(dateUntil);
                        String dateNowSt= REPEAT_TIME_FORMAT_SDF.format(now);
                        int intUntil=Integer.parseInt(dateUntilSt);
                        int intNow=Integer.parseInt(dateNowSt);
                        PreyLogger.d("Trigger TimeTrigger intUntil:"+intUntil);
                        PreyLogger.d("Trigger TimeTrigger intNow:"+intNow);
                        if(dateUntil!=null&&intNow>intUntil) {
                            PreyLogger.d("Trigger TimeTrigger The execution range dat");
                            throw new TriggerException(4,"The execution range dates doesn't make sense");
                        }
                    }
                    String hourSt = json.getString("hour");
                    String minuteSt = json.getString("minute");
                    String secondSt = json.getString("second");
                    PreyLogger.d("Trigger TimeTrigger hour:" + hourSt + " minute:" + minuteSt + " second:" + secondSt+" until:"+until);
                    int hour =0;
                    try{hour=Integer.parseInt(hourSt);}catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    int minute =0;
                    try{minute=Integer.parseInt(minuteSt);}catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    int second =0;
                    try{second=Integer.parseInt(secondSt);}catch (Exception e){
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                    Intent myIntent = new Intent(ctx, TimeTriggerReceiver.class);
                    myIntent.putExtra("trigger_id", "" + trigger.id);
                    String daysOfWeek = json.getString("days_of_week");
                    daysOfWeek=daysOfWeek.replace("[","");
                    daysOfWeek=daysOfWeek.replace("]","");
                    String[] parts = daysOfWeek.split(",");
                    for (int i = 0; parts != null && i < parts.length; i++) {
                        String daySt = parts[i];
                        int day=TriggerUtil.dayTrigger(daySt);
                        Calendar calender = Calendar.getInstance();
                        calender.set(Calendar.HOUR_OF_DAY, hour);
                        calender.set(Calendar.MINUTE, minute);
                        calender.set(Calendar.SECOND, 0);
                        calender.set(Calendar.MILLISECOND, 0);
                        PreyLogger.d("Trigger TimeTrigger array DAY_OF_WEEK:" + day);
                        calender.set(Calendar.DAY_OF_WEEK, day);
                        Intent intent = new Intent(ctx, TimeTriggerReceiver.class);
                        intent.putExtra("trigger_id", "" + trigger.id);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, (trigger.id * ADD_TRIGGER_ID  +   day), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                    }
                } catch (TriggerException te) {
                    throw te;
                } catch (Exception e) {
                    PreyLogger.e("error:" + e.getMessage(), e);
                    new TriggerException(0,"Unknown error:"+e.getMessage());
                }
            }
        }
    }

}