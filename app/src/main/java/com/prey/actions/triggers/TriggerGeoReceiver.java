/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;

import org.json.JSONObject;

import java.util.List;

public class TriggerGeoReceiver extends TriggerReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    public void onReceive(Context context, int geoId, String transition) {
        TriggerDataSource dataSource = new TriggerDataSource(context);
        List<TriggerDto> listTrigger = dataSource.getAllTriggers();
        PreyLogger.d("Trigger  onReceive geoId:" + geoId + " transition:" + transition);
        PreyLogger.d("Trigger  onReceive listTrigger.size():" + (listTrigger == null ? -1 : listTrigger.size()));
        for (int i = 0; listTrigger != null && i < listTrigger.size(); i++) {
            TriggerDto trigger = listTrigger.get(i);
            String triggerName = trigger.getName();
            PreyLogger.d("Trigger triggerName:" + triggerName);
            List<TriggerEventDto> listEvents = TriggerParse.TriggerEvents(trigger.getEvents());
            for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
                TriggerEventDto event = listEvents.get(j);
                if (transition.equals(event.type)) {
                    try {
                        JSONObject jsnobjectEvent = new JSONObject(event.info);
                        int eventGeoId = jsnobjectEvent.getInt("id");
                        PreyLogger.d("Trigger triggerName eventGeoId:" + eventGeoId);
                        if (eventGeoId == geoId) {
                            boolean process = true;
                            boolean haveRange = TriggerUtil.haveRange(listEvents);
                            if (haveRange) {
                                PreyLogger.d("Trigger TriggerReceiver  haveRange:" + haveRange);
                                boolean validRange = TriggerUtil.validRange(listEvents);
                                PreyLogger.d("Trigger TriggerReceiver  validRange:" + validRange);
                                process = validRange;
                            }
                            if (process) {
                                PreyLogger.d("Trigger triggerName trigger.getActions():" + trigger.getActions());
                                executeActions(context, trigger.getActions());
                            }
                        }
                    } catch (Exception e) {
                        PreyLogger.e("e:" + e.getMessage(), e);
                    }
                }
            }
        }
    }
}

