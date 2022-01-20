/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.aware.AwareConfig;
import com.prey.actions.fileretrieval.FileretrievalController;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.report.ReportScheduled;
import com.prey.activities.LoginActivity;
import com.prey.activities.WelcomeBatchActivity;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.preferences.RunBackgroundCheckBoxPreference;

import org.json.JSONObject;

import java.util.List;

public class Detach {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.i("Detach");
        boolean expired=false;
        try {
            expired = UtilJson.getBoolean(parameters, "expired");
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s" , e.getMessage()), e);
        }
        if(expired) {
            PreyConfig.getPreyConfig(ctx).setInstallationStatus("DEL");
            PreyLogger.d(String.format("Detach expired:%b" , expired));
            Detach.detachDevice(ctx,true,false,false,expired);
        }else {
            Detach.detachDevice(ctx);
        }
    }

    public static String detachDevice(Context ctx){
        return detachDevice(ctx,true,true,true,false);
    }

    public static String detachDevice(Context ctx,boolean openApplication,boolean removePermissions,boolean removeCache,boolean expired){
        PreyLogger.d("detachDevice");
        String error=null;
        try {
            PreyConfig.getPreyConfig(ctx).unregisterC2dm(false);
        } catch (Exception e) {
            error = e.getMessage();
        }
        try {
            PreyConfig.getPreyConfig(ctx).setSecurityPrivilegesAlreadyPrompted(false);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        PreyLogger.d(String.format("1:%s", error));
        try {   PreyConfig.getPreyConfig(ctx).setProtectAccount(false);} catch (Exception e) {error += e.getMessage();}
        try {   PreyConfig.getPreyConfig(ctx).setProtectPrivileges(false);} catch (Exception e) {error += e.getMessage();}
        try {   PreyConfig.getPreyConfig(ctx).setProtectTour(false);} catch (Exception e) {error += e.getMessage();}
        try {   PreyConfig.getPreyConfig(ctx).setProtectReady(false);} catch (Exception e) {error += e.getMessage();}
        PreyLogger.d(String.format("2:%s", error));
        try {
            if(removePermissions) {
                FroyoSupport fSupport = FroyoSupport.getInstance(ctx);
                if (fSupport.isAdminActive()) {
                    fSupport.removeAdminPrivileges();
                }
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        try {
            RunBackgroundCheckBoxPreference.notifyCancel(ctx);
            PreyConfig.getPreyConfig(ctx).removeLocationAware();
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        PreyLogger.d(String.format("3:%s", error));
        try {
            PreyConfig.getPreyConfig(ctx).setAware(false);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s" + e.getMessage()), e);
        }
        try {
            GeofenceController.getInstance().deleteAllZones(ctx);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        PreyLogger.d(String.format("4:%s", error));
        try {
            FileretrievalController.getInstance().deleteAll(ctx);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        PreyConfig.getPreyConfig(ctx).setPrefsBiometric(false);
        PreyLogger.d(String.format("5:%s", error));
        try { ReportScheduled.getInstance(ctx).reset();} catch (Exception e) {error += e.getMessage();}
        try {
            PreyWebServices.getInstance().deleteDevice(ctx);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        PreyLogger.d(String.format("6:%s", error));
        if(removeCache) {
            try {
                PreyConfig.getPreyConfig(ctx).wipeData();
            } catch (Exception e) {
                error += e.getMessage();
            }
        }
        try { PreyConfig.getPreyConfig(ctx).removeDeviceId();} catch (Exception e) {error += e.getMessage();}
        try { PreyConfig.getPreyConfig(ctx).removeEmail();} catch (Exception e) {error += e.getMessage();}
        try { PreyConfig.getPreyConfig(ctx).removeApiKey();} catch (Exception e) {PreyLogger.e("Error:"+e.getMessage(),e);}
        try { PreyConfig.getPreyConfig(ctx).setPinNumber("");} catch (Exception e) {error = e.getMessage();}
        try { PreyConfig.getPreyConfig(ctx).setEmail("");} catch (Exception e) {error = e.getMessage();}
        PreyLogger.d(String.format("7:%s", error));
        try { PreyConfig.getPreyConfig(ctx).setDeviceId("");} catch (Exception e) {error = e.getMessage();}
        try { PreyConfig.getPreyConfig(ctx).setApiKey("");} catch (Exception e) {error = e.getMessage();}
        PreyLogger.d(String.format("8:%s", error));
        if(!expired) {
            try {
                PreyConfig.getPreyConfig(ctx).setInstallationStatus("");
            } catch (Exception e) {
                error = e.getMessage();
            }
        }
        PreyLogger.d(String.format("Email:%s", PreyConfig.getPreyConfig(ctx).getEmail()));
        PreyLogger.d(String.format("DeviceId:%s", PreyConfig.getPreyConfig(ctx).getDeviceId()));
        PreyLogger.d(String.format("ApiKey:%s", PreyConfig.getPreyConfig(ctx).getApiKey()));
        if(removeCache) {
            try {
                PreyConfig.deleteCacheInstance(ctx);
            } catch (Exception e) {
                PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
            }
        }
        try {
            if(openApplication) {
                Intent intent = new Intent(ctx, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ctx.startActivity(intent);
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        return error;
    }
}
