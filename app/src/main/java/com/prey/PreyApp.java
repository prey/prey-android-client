/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import com.appsflyer.AppsFlyerConversionListener;
import com.google.firebase.FirebaseApp;
import com.prey.actions.aware.AwareController;
import com.prey.actions.fileretrieval.FileretrievalController;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.report.ReportScheduled;
import com.prey.actions.triggers.TriggerController;
import com.prey.events.factories.EventFactory;
import com.prey.events.receivers.EventReceiver;
import com.prey.net.PreyWebServices;
import com.prey.preferences.RunBackgroundCheckBoxPreference;
import com.prey.services.AwareJobService;
import com.prey.services.PreyDisablePowerOptionsService;

import java.util.Date;
import java.util.Map;

public class PreyApp extends Application {

    public long mLastPause;
    private EventReceiver eventReceiver = new EventReceiver();

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
            String apiKey = PreyConfig.getPreyConfig(ctx).getApiKey();
            String deviceKey = PreyConfig.getPreyConfig(ctx).getDeviceId();
            PreyLogger.d("apiKey:"+apiKey);
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
                       try {
                           if(android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP ) {
                               AwareJobService.schedule(ctx);
                           }
                       }catch (Exception e){}
                       try {FileretrievalController.getInstance().run(ctx);}catch (Exception e){}
                       try {TriggerController.getInstance().run(ctx);}catch (Exception e){}
                    }
                }.start();
                if (missing) {
                    if (PreyConfig.getPreyConfig(ctx).getIntervalReport() != null && !"".equals(PreyConfig.getPreyConfig(ctx).getIntervalReport())) {
                        ReportScheduled.getInstance(ctx).run();
                    }
                }
                if(PreyConfig.getPreyConfig(ctx).isRunBackground()){
                    RunBackgroundCheckBoxPreference.notifyReady(ctx);
                }
                if(PreyConfig.getPreyConfig(ctx).isDisablePowerOptions()){
                    try{
                        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                            ctx.startService(new Intent(ctx, PreyDisablePowerOptionsService.class));
                        }
                    }catch (Exception e){
                        PreyLogger.e( "error startService PreyDisablePowerOptionsService : " + e.getMessage(),e);
                    }
                }
                IntentFilter ACTION_POWER_CONNECTED = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
                registerReceiver(eventReceiver, ACTION_POWER_CONNECTED);
                IntentFilter ACTION_POWER_DISCONNECTED = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
                registerReceiver(eventReceiver, ACTION_POWER_DISCONNECTED);
                IntentFilter ACTION_BATTERY_LOW = new IntentFilter(Intent.ACTION_BATTERY_LOW);
                registerReceiver(eventReceiver, ACTION_BATTERY_LOW);
                IntentFilter ACTION_PROVIDER_CHANGED = new IntentFilter(Intent.ACTION_PROVIDER_CHANGED);
                registerReceiver(eventReceiver, ACTION_PROVIDER_CHANGED);
                IntentFilter LOCATION_MODE_CHANGED = new IntentFilter(EventFactory.LOCATION_MODE_CHANGED);
                registerReceiver(eventReceiver, LOCATION_MODE_CHANGED);
                IntentFilter CONNECTIVITY_CHANGE = new IntentFilter(EventFactory.CONNECTIVITY_CHANGE);
                registerReceiver(eventReceiver, CONNECTIVITY_CHANGE);
                IntentFilter LOCATION_PROVIDERS_CHANGED = new IntentFilter(EventFactory.LOCATION_PROVIDERS_CHANGED);
                registerReceiver(eventReceiver, LOCATION_PROVIDERS_CHANGED);
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
