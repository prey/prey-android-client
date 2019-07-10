/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.util.ClassUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class TriggerReceiver extends BroadcastReceiver {

    public abstract void onReceive(Context context, Intent intent);

    public void execute(Context context, String name) {
        TriggerDataSource dataSource = new TriggerDataSource(context);
        List<TriggerDto> listTrigger = dataSource.getAllTriggers();
        PreyLogger.d("Trigger TriggerReceiver onReceive name:" + name);
        PreyLogger.d("Trigger TriggerReceiver onReceive listTrigger.size():" + (listTrigger == null ? -1 : listTrigger.size()));
        for (int i = 0; listTrigger != null && i < listTrigger.size(); i++) {
            TriggerDto trigger = listTrigger.get(i);
            List<TriggerEventDto> listEvents = TriggerParse.TriggerEvents(trigger.getEvents());
            String triggerName = trigger.getName();
            PreyLogger.d("Trigger TriggerReceiver triggerName:" + triggerName);
            for (int j = 0; listEvents != null && j < listEvents.size(); j++) {
                TriggerEventDto event = listEvents.get(j);
                if (name.equals(event.type)) {
                    boolean process = true;
                    boolean haveRange = TriggerUtil.haveRange(listEvents);
                    if (haveRange) {
                        PreyLogger.d("Trigger TriggerReceiver  haveRange:" + haveRange);
                        boolean validRange = TriggerUtil.validRange(listEvents);
                        PreyLogger.d("Trigger TriggerReceiver  validRange:" + validRange);
                        process = validRange;
                    }
                    try {
                        if (process) {
                            PreyLogger.d("Trigger TriggerReceiver triggerName trigger.getActions():" + trigger.getActions());
                            executeActions(context, trigger.getActions());
                        }
                    } catch (Exception e) {
                        PreyLogger.e("e:" + e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void executeActions(final Context context, String actions) throws Exception {
        List<TriggerActionDto> listActions = TriggerParse.TriggerActions(actions);
        for (int z = 0; listActions != null && z < listActions.size(); z++) {
            final TriggerActionDto actionDto = listActions.get(z);
            int delay = actionDto.delay;
            PreyLogger.d("triggerName TriggerReceiver delay:" + delay);
            if (delay > 0) {
                Thread.sleep(delay * 1000);
            }
            new Thread() {
                public void run() {
                    try {
                        PreyLogger.d("Trigger triggerName actionDto.action:" + actionDto.action);
                        JSONObject jsonObject = new JSONObject(actionDto.action);
                        PreyLogger.d("Trigger triggerName action:" + jsonObject);
                        PreyLogger.d("Trigger triggerName jsonObject:" + jsonObject);
                        String nameAction = jsonObject.getString("target");
                        PreyLogger.d("Trigger triggerName nameAction:" + nameAction);
                        String methodAction = jsonObject.getString("command");
                        PreyLogger.d("Trigger triggerName methodAction:" + methodAction);
                        JSONObject parametersAction = null;
                        try {
                            parametersAction = jsonObject.getJSONObject("options");
                            PreyLogger.d("Trigger triggerName parametersAction:" + parametersAction);
                        } catch (JSONException e) {
                        }
                        if (parametersAction == null) {
                            parametersAction = new JSONObject();
                        }
                        try {
                            String messageId = jsonObject.getString(PreyConfig.MESSAGE_ID);
                            parametersAction.put(PreyConfig.MESSAGE_ID, messageId);
                        } catch (Exception e) {
                        }
                        PreyLogger.d("Trigger nameAction:" + nameAction + " methodAction:" + methodAction + " parametersAction:" + parametersAction);
                        List<ActionResult> lista = new ArrayList<ActionResult>();
                        ClassUtil.execute(context, lista, nameAction, methodAction, parametersAction, null);
                    } catch (Exception e) {
                        PreyLogger.e("Trigger error:" + e.getMessage(), e);
                    }
                }
            }.start();
        }
    }
}
