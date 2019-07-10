/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.net.PreyWebServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TriggerParse {

    public static List<TriggerDto> getJSONFromUrl(Context ctx) {
        String json = null;
        try {
            json = PreyWebServices.getInstance().triggers(ctx);
            return getJSONFromTxt(ctx, json);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<TriggerDto> getJSONFromTxt(Context ctx, String json) {
        json = "{\"prey\":" + json + "}";
        List<TriggerDto> listTrigger = new ArrayList<TriggerDto>();
        PreyLogger.d(json);
        try {
            JSONObject jsnobject = new JSONObject(json);
            JSONArray jsonArray = jsnobject.getJSONArray("prey");
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonCommand = jsonArray.get(i).toString();
                JSONObject explrObject = new JSONObject(jsonCommand);
                TriggerDto trigger = new TriggerDto();
                try {
                    trigger.id = explrObject.getInt("id");
                } catch (Exception e) {
                    trigger.id = 101;
                }
                try {
                    trigger.name = explrObject.getString("name");
                } catch (Exception e) {
                    trigger.name = "ups";
                }
                trigger.events = explrObject.getString("automation_events");
                trigger.actions = explrObject.getString("automation_actions");
                listTrigger.add(trigger);
            }
        } catch (Exception e) {
            PreyLogger.e("e:" + e.getMessage(), e);
            return null;
        }
        return listTrigger;
    }

    public static List<TriggerEventDto> TriggerEvents(String events) {
        events = "{\"prey\":" + events + "}";
        List<TriggerEventDto> listTrigger = new ArrayList<TriggerEventDto>();
        PreyLogger.d(events);
        try {
            JSONObject jsnobject = new JSONObject(events);
            JSONArray jsonArray = jsnobject.getJSONArray("prey");
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonCommand = jsonArray.get(i).toString();
                JSONObject explrObject = new JSONObject(jsonCommand);
                TriggerEventDto trigger = new TriggerEventDto();
                trigger.type = explrObject.getString("type");
                trigger.info = explrObject.getString("info");
                listTrigger.add(trigger);
            }
        } catch (Exception e) {
            return null;
        }
        return listTrigger;
    }

    public static List<TriggerActionDto> TriggerActions(String actions) {
        actions = "{\"prey\":" + actions + "}";
        List<TriggerActionDto> listTrigger = new ArrayList<TriggerActionDto>();
        PreyLogger.d(actions);
        try {
            JSONObject jsnobject = new JSONObject(actions);
            JSONArray jsonArray = jsnobject.getJSONArray("prey");
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonCommand = jsonArray.get(i).toString();
                JSONObject explrObject = new JSONObject(jsonCommand);
                TriggerActionDto trigger = new TriggerActionDto();
                trigger.delay = explrObject.getInt("delay");
                trigger.action = explrObject.getString("action");
                listTrigger.add(trigger);
            }
        } catch (Exception e) {
            return null;
        }
        return listTrigger;
    }

}