/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import java.util.HashMap;

import android.content.Context;

import com.prey.PreyConfig;

public class ReportActionResponse {

    private HashMap<String, PreyAction> actionsToPerform = new HashMap<String, PreyAction>();
    private boolean missing = false;
    private long delay = 60;
    private String postUrl;

    public void addAction(String actionName, String active) {

        PreyAction action = PreyAction.getActionFromName(actionName);
        if (action != null)
            this.actionsToPerform.put(actionName, action);
    }

    public void addActionConfigParameter(String name, String parameterName, String parameterValue) {
        actionsToPerform.get(name).getConfig().put(parameterName, parameterValue);
    }

    public HashMap<String, PreyAction> getActionsToPerform(Context ctx) {
        if (PreyConfig.getPreyConfig(ctx).isCamouflageSet()){
            if(!actionsToPerform.containsKey("camouflage")){
                addAction("uncamouflage", "");
            }
        }
        return actionsToPerform;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public boolean isMissing() {
        return missing;
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }



}
