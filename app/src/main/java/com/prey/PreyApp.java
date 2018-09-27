/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.Application;
import android.content.Intent;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.firebase.FirebaseApp;
import com.prey.actions.aware.AwareController;
import com.prey.actions.fileretrieval.FileretrievalController;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.report.ReportScheduled;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.managers.PreyTelephonyManager;
import com.prey.net.PreyWebServices;
import com.prey.preferences.DisablePowerCheckBoxPreference;
import com.prey.services.PreyDisablePowerOptionsService;

import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

public class PreyApp extends Application {

    public long mLastPause;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mLastPause = 0;
            PreyLogger.d("__________________");
            PreyLogger.i("Application launched!");
            PreyLogger.d("__________________");

            boolean chromium=getPackageManager().hasSystemFeature("org.chromium.arc.device_management");
            PreyLogger.d("chromium:"+chromium);

            String deviceKey = PreyConfig.getPreyConfig(this).getDeviceId();

            PreyLogger.d("InstallationDate:" + PreyConfig.getPreyConfig(this).getInstallationDate());
            if (PreyConfig.getPreyConfig(this).getInstallationDate() == 0) {
                PreyConfig.getPreyConfig(this).setInstallationDate(new Date().getTime());
                PreyWebServices.getInstance().sendEvent(this, PreyConfig.ANDROID_INIT);
            }
            String sessionId = PreyUtils.randomAlphaNumeric(16);
            PreyLogger.d("#######sessionId:" + sessionId);
            PreyConfig.getPreyConfig(this).setSessionId(sessionId);

            boolean missing=PreyConfig.getPreyConfig(this).isMissing();

            if (deviceKey != null && deviceKey != "") {
                new Thread() {
                    public void run() {
                        PreyConfig.getPreyConfig(getApplicationContext()).registerC2dm();
                        try {
                            String email = PreyWebServices.getInstance().getEmail(getApplicationContext());
                            PreyConfig.getPreyConfig(getApplicationContext()).setEmail(email);
                        }catch (Exception e){}
                        GeofenceController.getInstance().run(getApplicationContext());
                        AwareController.getInstance().init(getApplicationContext());
                        FileretrievalController.getInstance().run(getApplicationContext());
                    }
                }.start();
                if (missing) {
                    if (PreyConfig.getPreyConfig(this).getIntervalReport() != null && !"".equals(PreyConfig.getPreyConfig(this).getIntervalReport())) {
                        ReportScheduled.getInstance(this).run();
                    }
                }
                new Thread() {
                    public void run() {
                        if (PreyConfig.getPreyConfig(getApplicationContext()).isSimChanged()) {
                            JSONObject info = new JSONObject();
                            try {
                                String lineNumber=PreyTelephonyManager.getInstance(getApplicationContext()).getLine1Number();
                                if(lineNumber!=null&&!"".equals(lineNumber)) {
                                    info.put("new_phone_number", lineNumber);
                                }
                                String simSerialNumber=PreyConfig.getPreyConfig(getApplicationContext()).getSimSerialNumber();
                                if(simSerialNumber!=null&&!"".equals(simSerialNumber)) {
                                    info.put("sim_serial_number", simSerialNumber);
                                }
                            } catch (Exception e) {
                            }
                            Event event= new Event(Event.SIM_CHANGED, info.toString());
                            new EventManagerRunner(getApplicationContext(), event).run(); ;
                        }
                    }
                }.start();

                if(PreyConfig.getPreyConfig(this).isDisablePowerOptions()){
                    DisablePowerCheckBoxPreference.notifyReady(this);
                    try{this.startService(new Intent(this, PreyDisablePowerOptionsService.class));}catch (Exception e){}
                }
                /*
                new Thread() {
                    public void run() {
                    AutoConnectScheduled.getInstance(getApplicationContext()).run();
                    }
                }.start();*/

            }
            try {
                FirebaseApp.initializeApp(this);
                AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
                    @Override
                    public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
                        for (String attrName : conversionData.keySet()) {
                            PreyLogger.d("attribute: " + attrName + " = " + conversionData.get(attrName));
                        }
                        setInstallData(conversionData);
                    }

                    @Override
                    public void onInstallConversionFailure(String errorMessage) {
                        PreyLogger.i( "error getting conversion data: " + errorMessage);
                    }

                    @Override
                    public void onAppOpenAttribution(Map<String, String> conversionData) {
                        for (String attrName : conversionData.keySet()) {
                            PreyLogger.d( "attribute: " + attrName + " = " + conversionData.get(attrName));
                        }
                    }

                    @Override
                    public void onAttributionFailure(String errorMessage) {
                        PreyLogger.d( "error onAttributionFailure : " + errorMessage);
                    }
                };

                String flyerKey= FileConfigReader.getInstance(getApplicationContext()).getFlyerKey();
                AppsFlyerLib.getInstance().init(flyerKey , conversionListener , getApplicationContext());
                AppsFlyerLib.getInstance().startTracking(this, flyerKey);
                AppsFlyerLib.getInstance().setDebugLog(false);
            }
            catch (Exception e) {
                PreyLogger.e("Error PreyApp:"+e.getMessage(),e);
            }
        } catch (Exception e) {
            PreyLogger.e("Error PreyApp:" + e.getMessage(), e);
        }
    }


    public static String InstallConversionData =  "";
    public static int sessionCount = 0;

    public static void setInstallData(Map<String, String> conversionData){
        if(sessionCount == 0){
            final String install_type = "Install Type: " + conversionData.get("af_status") + "\n";
            final String media_source = "Media Source: " + conversionData.get("media_source") + "\n";
            final String install_time = "Install Time(GMT): " + conversionData.get("install_time") + "\n";
            final String click_time = "Click Time(GMT): " + conversionData.get("click_time") + "\n";
            final String is_first_launch = "Is First Launch: " + conversionData.get("is_first_launch") + "\n";
            InstallConversionData += install_type + media_source + install_time + click_time + is_first_launch;
            sessionCount++;
        }
    }

}
