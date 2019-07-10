/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import android.content.Context;

import com.prey.PreyLogger;

import java.util.List;

public class TriggerDataSource {

    private TriggerOpenHelper dbHelper;

    public TriggerDataSource(Context context) {
        dbHelper = new TriggerOpenHelper(context);
    }

    public void createTrigger(TriggerDto trigger) {
        try {
            dbHelper.insertTrigger(trigger);
        } catch (Exception e) {
            try {
                dbHelper.updateTrigger(trigger);
            } catch (Exception e1) {
                PreyLogger.e("Trigger error db update:" + e1.getMessage(), e1);
            }
        }
    }

    public void deleteTrigger(String id) {
        dbHelper.deleteTrigger(id);
    }

    public List<TriggerDto> getAllTriggers() {
        return dbHelper.getAllTriggers();
    }

    public TriggerDto getTrigger(String id) {
        return dbHelper.getTrigger(id);
    }

    public void deleteAllTrigger() {
        dbHelper.deleteAllTrigger();
    }

    public void updateTrigger(TriggerDto trigger) {
        dbHelper.updateTrigger(trigger);
    }
}