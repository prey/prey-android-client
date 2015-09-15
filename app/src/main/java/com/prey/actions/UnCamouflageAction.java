/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.actions.camouflage.Camouflage;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;
import com.prey.exceptions.PreyException;

public class UnCamouflageAction extends PreyAction {

    public static final String DATA_ID = "uncamouflage";
    public final String ID = "uncamouflage";

    @Override
    public String textToNotifyUserOnEachReport(Context ctx) {
        return "";
    }

    @Override
    public boolean shouldNotify() {
        return false;
    }

    @Override
    public void execute(ActionJob actionJob, Context ctx) throws PreyException {
        List<ActionResult> lista=null;
        JSONObject parameters=null;
        Camouflage.unhide(ctx, lista, parameters);
    }

    @Override
    public boolean isSyncAction() {
        return false;
    }

    @Override
    public int getPriority() {
        return UN_CAMOUFLAGE_PRIORITY;
    }

}

