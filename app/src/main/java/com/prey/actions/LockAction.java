/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionJob;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.exceptions.PreyException;

public class LockAction extends PreyAction {

    public static final String DATA_ID = "lock";
    public final String ID = "lock";

    public HttpDataService run(Context ctx) {
        return null;
    }

    @Override
    public String textToNotifyUserOnEachReport(Context ctx) {
        return "";
    }

    @Override
    public void execute(ActionJob actionJob, Context ctx) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        if (preyConfig.isFroyoOrAbove()){
            preyConfig.setLock(true);
            try{
                FroyoSupport.getInstance(ctx).changePasswordAndLock(getConfig().get("unlock_pass"),true);
            }catch (PreyException e){
            }

        }
    }

    @Override
    public boolean isSyncAction() {
        return false;
    }

    @Override
    public boolean shouldNotify() {
        return false;
    }

    @Override
    public void killAnyInstanceRunning(Context ctx) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        if (preyConfig.isFroyoOrAbove()) {
            PreyLogger.d("-- Unlock instruction received");
            try{
                FroyoSupport.getInstance(ctx).changePasswordAndLock("",true);
            }catch (PreyException e){
            }
        }
    }

    public int getPriority(){
        return LOCK_PRIORITY;
    }

}

