/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;

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
import com.prey.preferences.RunBackgroundCheckBoxPreference;
import com.prey.services.PreyDisablePowerOptionsService;

import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

public class PreyApp extends Application {

    public long mLastPause;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        boolean chromium=getPackageManager().hasSystemFeature("org.chromium.arc.device_management");
        PreyLogger.d("chromium:"+chromium);
        run(this);

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
                    PreyLogger.d( "error getting conversion data: " + errorMessage);
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


        }
        catch (Exception e) {
            PreyLogger.e("Error PreyApp:"+e.getMessage(),e);
        }

    }

    public void run(final Context ctx) {
        try {
            mLastPause = 0;
            PreyLogger.d("__________________");
            PreyLogger.i("Application launched!");
            PreyLogger.d("__________________");
            PreyConfig.getPreyConfig(ctx).setReportNumber(0);


            String deviceKey = PreyConfig.getPreyConfig(ctx).getDeviceId();

            PreyLogger.d("deviceKey:"+deviceKey);
            PreyLogger.d("InstallationDate:" + PreyConfig.getPreyConfig(ctx).getInstallationDate());
            if (PreyConfig.getPreyConfig(ctx).getInstallationDate() == 0) {
                PreyConfig.getPreyConfig(ctx).setInstallationDate(new Date().getTime());
                PreyWebServices.getInstance().sendEvent(ctx, PreyConfig.ANDROID_INIT);
            }
            String sessionId = PreyUtils.randomAlphaNumeric(16);
            PreyLogger.d("#######sessionId:" + sessionId);
            PreyConfig.getPreyConfig(ctx).setSessionId(sessionId);

            boolean missing=PreyConfig.getPreyConfig(ctx).isMissing();


            if (deviceKey != null && deviceKey != "") {
                new Thread() {
                    public void run() {
                       try {
                           PreyConfig.getPreyConfig(ctx).registerC2dm();
                       }catch (Exception e){
                           PreyLogger.e("registerC2dm error:"+e.getMessage(),e);
                       }
                       try {
                            String email = PreyWebServices.getInstance().getEmail(ctx);
                            PreyConfig.getPreyConfig(ctx).setEmail(email);
                       }catch (Exception e){
                           PreyLogger.e("setEmail error:"+e.getMessage(),e);
                       }
                        try {PreyStatus.getInstance().getConfig(ctx);}catch (Exception e){}
                        try {GeofenceController.getInstance().run(ctx);}catch (Exception e){}
                        try {AwareController.getInstance().init(ctx);}catch (Exception e){}
                        try {FileretrievalController.getInstance().run(ctx);}catch (Exception e){}


                    }
                }.start();

                if (missing) {
                    if (PreyConfig.getPreyConfig(ctx).getIntervalReport() != null && !"".equals(PreyConfig.getPreyConfig(ctx).getIntervalReport())) {
                        ReportScheduled.getInstance(ctx).run();
                    }
                }
                new Thread() {
                    public void run() {
                        if (PreyConfig.getPreyConfig(ctx).isSimChanged()) {
                            JSONObject info = new JSONObject();
                            try {
                                String lineNumber=PreyTelephonyManager.getInstance(ctx).getLine1Number();
                                if(lineNumber!=null&&!"".equals(lineNumber)) {
                                    info.put("new_phone_number", lineNumber);
                                }
                                String simSerialNumber=PreyConfig.getPreyConfig(ctx).getSimSerialNumber();
                                if(simSerialNumber!=null&&!"".equals(simSerialNumber)) {
                                    info.put("sim_serial_number", simSerialNumber);
                                }
                            } catch (Exception e) {
                            }
                            Event event= new Event(Event.SIM_CHANGED, info.toString());
                            new EventManagerRunner(ctx, event).run(); ;
                        }
                    }
                }.start();

                if(PreyConfig.getPreyConfig(ctx).isRunBackground()){
                    RunBackgroundCheckBoxPreference.notifyReady(ctx);
                }

                if(PreyConfig.getPreyConfig(ctx).isDisablePowerOptions()){
                    try{
                        if(android.os.Build.VERSION.SDK_INT < PreyConfig.VERSION_CODES_P) {
                            ctx.startService(new Intent(ctx, PreyDisablePowerOptionsService.class));
                        }
                    }catch (Exception e){
                        PreyLogger.e( "error startService PreyDisablePowerOptionsService : " + e.getMessage(),e);
                    }
                }



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
