/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.content.Context;

import com.prey.net.PreyWebServices;

import org.json.JSONObject;

public class PreyStatus {

    private PreyStatus() {
    }

    private static PreyStatus instance = null;

    public static PreyStatus getInstance() {
        if (instance == null) {
            instance = new PreyStatus();
        }
        return instance;
    }

    private boolean preyConfigurationActivityResume = false;

    private boolean preyPopUpOnclick = false;

    private boolean isTakenPicture = false;

    private boolean isAlarmStart = false;

    public boolean isPreyConfigurationActivityResume() {
        return preyConfigurationActivityResume;
    }

    public void setPreyConfigurationActivityResume(
            boolean preyConfigurationActivityResume) {
        this.preyConfigurationActivityResume = preyConfigurationActivityResume;
    }

    public boolean isAlarmStart() {
        return isAlarmStart;
    }

    public void setAlarmStart() {
        this.isAlarmStart = true;
    }

    public void setAlarmStop() {
        this.isAlarmStart = false;
    }

    public boolean isTakenPicture() {
        return isTakenPicture;
    }

    public void setTakenPicture(boolean isTakenPicture) {
        this.isTakenPicture = isTakenPicture;
    }

    public boolean isPreyPopUpOnclick() {
        return preyPopUpOnclick;
    }

    public void setPreyPopUpOnclick(boolean preyPopUpOnclick) {
        this.preyPopUpOnclick = preyPopUpOnclick;
    }

    /**
     * Method initialize device state
     * @param ctx
     */
    public void initConfig(Context ctx){
        boolean aware = false;
        boolean autoconnect = false;
        int minutesToQueryServer;
        try {
            JSONObject jsnobject = PreyWebServices.getInstance().getStatus(ctx);
            if (jsnobject != null) {
                PreyLogger.d("STATUS jsnobject :" + jsnobject);
                JSONObject jsnobjectSettings = jsnobject.getJSONObject("settings");
                try {
                    JSONObject jsnobjectLocal = jsnobjectSettings.getJSONObject("local");
                    aware = jsnobjectLocal.getBoolean("location_aware");
                }catch(Exception e){
                    aware = false;
                }
                try {
                    JSONObject jsnobjectGlobal = jsnobjectSettings.getJSONObject("global");
                    autoconnect = jsnobjectGlobal.getBoolean("auto_connect");
                }catch(Exception e){
                    autoconnect =false;
                }
                PreyConfig.getPreyConfig(ctx).setAware(aware);
                PreyConfig.getPreyConfig(ctx).setAutoConnect(autoconnect);
                PreyLogger.d(String.format("STATUS aware :%b", aware));
                PreyLogger.d(String.format("STATUS autoconnect :%b", autoconnect));
                try {
                    minutesToQueryServer = jsnobject.getInt("minutes_to_query_server");
                } catch (Exception e) {
                    minutesToQueryServer = PreyConfig.getPreyConfig(ctx).getMinutesToQueryServer();
                }
                PreyConfig.getPreyConfig(ctx).setMinutesToQueryServer(minutesToQueryServer);
                PreyLogger.d(String.format("STATUS minutesToQueryServer :%s", minutesToQueryServer));
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("STATUS Error:%s", e.getMessage()), e);
        }
    }

}