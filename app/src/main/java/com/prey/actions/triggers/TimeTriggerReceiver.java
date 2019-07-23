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

public class TimeTriggerReceiver extends TriggerReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PreyLogger.d("Trigger TimeTriggerReceiver:");
        try {
            final String triggerId = intent.getExtras().getString("trigger_id");
            PreyLogger.d("Trigger Receiver trigger_id:" + triggerId);
            TriggerDataSource dataSource = new TriggerDataSource(context);
            TriggerDto trigger = dataSource.getTrigger(triggerId);
            if (trigger != null) {
                String triggerName = trigger.getName();
                boolean validate=TriggerUtil.validateTrigger(trigger);
                if(validate) {
                    PreyLogger.d("Trigger triggerName:" + triggerName);
                    PreyLogger.d("Trigger triggerName trigger.getActions():" + trigger.getActions());
                    executeActions(context, trigger.getActions());
                }
            }
        } catch (Exception e) {
            PreyLogger.e("Trigger error:" + e.getMessage(), e);
        }
    }
}