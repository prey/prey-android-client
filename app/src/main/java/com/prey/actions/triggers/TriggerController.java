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

import java.util.List;

public class TriggerController {

    private static TriggerController instance;

    public static TriggerController getInstance() {
        if (instance == null) {
            instance = new TriggerController();
        }
        return instance;
    }

    public void run(Context ctx) {
        TriggerDataSource dataSource = new TriggerDataSource(ctx);
        cancelAlarm(ctx, dataSource);
        dataSource.deleteAllTrigger();
        try {
            List<TriggerDto> listWeb = TriggerParse.getJSONFromUrl(ctx);
            for (int i = 0; listWeb != null && i < listWeb.size(); i++) {
                TriggerDto trigger = listWeb.get(i);
                if (dataSource.getTrigger("" + trigger.getId()) != null) {
                    dataSource.updateTrigger(trigger);
                } else {
                    dataSource.createTrigger(trigger);
                }
                TimeTrigger.updateTrigger(ctx, trigger);
            }
        } catch (Exception e) {
            PreyLogger.e("e:" + e.getMessage(), e);
        }
    }

    public void cancelAlarm(Context ctx, TriggerDataSource dataSource) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
        List<TriggerDto> list = dataSource.getAllTriggers();
        for (int i = 0; list != null && i < list.size(); i++) {
            TriggerDto trigger = list.get(i);
            String events = trigger.getEvents();
            if (events.indexOf("exact_time") > 0) {
                Intent intent = new Intent(ctx, TimeTriggerReceiver.class);
                intent.putExtra("trigger_id", "" + trigger.id);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, (trigger.id + TimeTrigger.ADD_TRIGGER_ID), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
            }
            if (events.indexOf("repeat_time") > 0) {
                JSONArray array = null;
                List<TriggerEventDto> listEvents = TriggerParse.TriggerEvents(events);
                for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
                    TriggerEventDto event = listEvents.get(j);
                    if ("repeat_time".equals(event.type)) {
                        try {
                            JSONObject json = new JSONObject(event.info);
                            array = json.getJSONArray("days_of_week");
                            for (int x = 0; array != null && x < array.length(); x++) {
                                int day = array.getInt(x);
                                Intent intent = new Intent(ctx, TimeTriggerReceiver.class);
                                intent.putExtra("trigger_id", "" + trigger.id);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, (trigger.id + TimeTrigger.ADD_TRIGGER_ID) + (day * TimeTrigger.MUL_TRIGGER_DAY), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                alarmManager.cancel(pendingIntent);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }
}