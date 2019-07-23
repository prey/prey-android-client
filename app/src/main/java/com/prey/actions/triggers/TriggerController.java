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
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TriggerController {

    private List<TriggerDto> listBD = null;
    private List<TriggerDto> listWeb = null;

    private static TriggerController instance;

    public static TriggerController getInstance() {
        if (instance == null) {
            instance = new TriggerController();
        }
        return instance;
    }

    public void run(Context ctx) {
        try {
            Thread.sleep(1000);
            TriggerDataSource dataSource = new TriggerDataSource(ctx);
            listBD = dataSource.getAllTriggers();
            listWeb =null;
            try {listWeb = TriggerParse.getJSONFromUrl(ctx); } catch (Exception e) {}
            updateTriggers(ctx,listWeb,listBD,dataSource);
        } catch (Exception e) {
            PreyLogger.e("error run"+e.getMessage(),e);
        }
    }

    private void updateTriggers(Context ctx, List<TriggerDto> listWeb , List<TriggerDto> listBD, TriggerDataSource dataSource){
        try {
            List<TriggerDto> listDelete=new ArrayList<>();
            List<TriggerDto> listUpdate=new ArrayList<>();
            Map<Integer, TriggerDto> mapBD = convertMap(listBD);
            Map<Integer, TriggerDto> mapWeb = convertMap(listWeb);
            List<Integer> removeList = new ArrayList<Integer>();
            List<Integer> listRemove = new ArrayList<Integer>();
            List<TriggerDto> listAdd = new ArrayList<TriggerDto>();
            List<TriggerDto> listRun= new ArrayList<TriggerDto>();
            List<TriggerDto> listDel = new ArrayList<TriggerDto>();
            List<TriggerDto> listStop = new ArrayList<TriggerDto>();
            for(int i=0;listBD!=null&&i<listBD.size();i++){
                TriggerDto dto=listBD.get(i);
                if(mapWeb!=null&&!mapWeb.containsKey(dto.getId())){
                    removeList.add(dto.getId());
                    listRemove.add(dto.getId());
                    listDel.add(dto);
                    dataSource.deleteTrigger(""+dto.getId());
                }
            }
            if(listDel.size()>0) {
                cancelAlarm(ctx,listDel);
            }
            if (removeList != null && removeList.size() > 0) {
                String infoDelete = "[";
                for (int i = 0; removeList != null && i < removeList.size(); i++) {
                    infoDelete += removeList.get(i);
                    if (i + 1 < removeList.size()) {
                        infoDelete += ",";
                    }
                }
                infoDelete += "]";
                PreyLogger.d("Trigger infoDelete:" + infoDelete);
                sendNotify(ctx, UtilJson.makeMapParam("start", "triggers", "stopped", infoDelete));
            }
            for(int i=0;listWeb!=null&&i<listWeb.size();i++) {
                TriggerDto tri = listWeb.get(i);
                if(mapBD.containsKey(tri.getId())){
                    listRun.add(tri);
                }else{
                    listAdd.add(tri);
                }
            }
            for(int i=0;listRun!=null&&i<listRun.size();i++) {
                TriggerDto trigger = listRun.get(i);
                try {
                    TimeTrigger.updateTrigger(ctx, trigger);
                }catch (TriggerException te) {
                    listStop.add(trigger);
                    PreyLogger.d("TimeTrigger listRun exception id:"+trigger.id+" ,state:"+te.code);
                }
            }
            String infoStop = "[";
            for(int i=0;listStop!=null&&i<listStop.size();i++) {
                TriggerDto trigger = listStop.get(i);
                infoStop += "{\"id\":"+trigger.id+",\"state\":3}";
                if (i + 1 < listStop.size()) {
                    infoStop += ",";
                }
                dataSource.deleteTrigger(""+trigger.id);
            }
            infoStop += "]";
            if(listStop!=null&&listStop.size()>0) {
                sendNotify(ctx, UtilJson.makeMapParam("start", "triggers", "started", infoStop));
            }
            String infoAdd = "[";
            for(int i=0;listAdd!=null&&i<listAdd.size();i++) {
                TriggerDto trigger = listAdd.get(i);
                try {
                    TimeTrigger.updateTrigger(ctx, trigger);
                    infoAdd += "{\"id\":"+trigger.id+",\"state\":1}";
                    dataSource.createTrigger(trigger);
                }catch (TriggerException te){
                    infoAdd += "{\"id\":"+trigger.id+",\"state\":"+te.code+"}";
                }
                if (i + 1 < listAdd.size()) {
                    infoAdd += ",";
                }
            }
            infoAdd += "]";
            if(listAdd!=null&&listAdd.size()>0) {
                sendNotify(ctx, UtilJson.makeMapParam("start", "triggers", "started", infoAdd));
            }
        } catch (Exception e) {
            PreyLogger.e("error run"+e.getMessage(),e);
        }
    }

    private Map<Integer, TriggerDto> convertMap(List<TriggerDto> list) {
        if(list==null){
            return null;
        }
        Map<Integer, TriggerDto> map = new HashMap<Integer, TriggerDto>();
        for (int i = 0; i < list.size(); i++) {
            TriggerDto tri = list.get(i);
            map.put(tri.getId(), tri);
        }
        return map;
    }


    public void cancelAlarm(Context ctx, List<TriggerDto> list) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
        for (int i = 0; list != null && i < list.size(); i++) {
            TriggerDto trigger = list.get(i);
            String events = trigger.getEvents();
            if (events.indexOf(TimeTrigger.EXACT_TIME) > 0) {
                Intent intent = new Intent(ctx, TimeTriggerReceiver.class);
                intent.putExtra("trigger_id", "" + trigger.id);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, (trigger.id * TimeTrigger.ADD_TRIGGER_ID), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
            }
            if (events.indexOf(TimeTrigger.REPEAT_TIME) > 0) {
                List<TriggerEventDto> listEvents = TriggerParse.TriggerEvents(events);
                for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
                    TriggerEventDto event = listEvents.get(j);
                    if ("repeat_time".equals(event.type)) {
                        try {
                            JSONObject json = new JSONObject(event.info);
                            JSONArray array = json.getJSONArray("days_of_week");
                            for (int x = 0; array != null && x < array.length(); x++) {
                                int day = array.getInt(x);
                                Intent intent = new Intent(ctx, TimeTriggerReceiver.class);
                                intent.putExtra("trigger_id", "" + trigger.id);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, (trigger.id * TimeTrigger.ADD_TRIGGER_ID + day), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                alarmManager.cancel(pendingIntent);
                            }
                        } catch (Exception e) {
                            PreyLogger.e("Error cancelAlarm:"+e.getMessage(),e);
                        }
                    }
                }
            }
        }
    }

    public void sendNotify(final Context ctx, final Map<String, String> params) {
        new Thread() {
            public void run() {
                try{
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, params);
                } catch (Exception e) {
                }
            }
        }.start();
    }
}