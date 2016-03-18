/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.fileretrieval.FileretrievalController;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.List;

public class Detach {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.i("Detach device");
        Detach.detachDevice(ctx);
    }

    public static String detachDevice(Context ctx){
        String error=null;
        try {
            PreyConfig.getPreyConfig(ctx).unregisterC2dm(false); } catch (Exception e) { error = e.getMessage();}
        try {   PreyConfig.getPreyConfig(ctx).setSecurityPrivilegesAlreadyPrompted(false);} catch (Exception e) {}


        try {   PreyConfig.getPreyConfig(ctx).setProtectAccount(false);} catch (Exception e) {error = e.getMessage();}
        try {   PreyConfig.getPreyConfig(ctx).setProtectPrivileges(false);} catch (Exception e) {error = e.getMessage();}
        try {   PreyConfig.getPreyConfig(ctx).setProtectTour(false);} catch (Exception e) {error = e.getMessage();}
        try {   PreyConfig.getPreyConfig(ctx).setProtectReady(false);} catch (Exception e) {error = e.getMessage();}

        try {
            FroyoSupport fSupport = FroyoSupport.getInstance(ctx);
            if (fSupport.isAdminActive()) {
                fSupport.removeAdminPrivileges();
            }
        } catch (Exception e) {}

        try {
            GeofenceController.getInstance().deleteAllZones(ctx);
        } catch (Exception e) {}

        try {
            FileretrievalController.getInstance().deleteAll(ctx);
        } catch (Exception e) {}

        try {  PreyWebServices.getInstance().deleteDevice(ctx);} catch (Exception e) {error = e.getMessage();}
        try {    PreyConfig.getPreyConfig(ctx).wipeData();} catch (Exception e) {error = e.getMessage();}
        return error;
    }
}
