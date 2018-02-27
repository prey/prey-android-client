/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.Application;

import com.prey.actions.aware.AwareConfig;
import com.prey.actions.fileretrieval.FileretrievalController;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.report.ReportScheduled;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.managers.PreyTelephonyManager;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.Date;

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
            String PreyVersion = PreyConfig.getPreyConfig(this).getPreyVersion();
            String preferencePreyVersion = PreyConfig.getPreyConfig(this).getPreferencePreyVersion();
            PreyLogger.d("PreyVersion:" + PreyVersion+" preferencePreyVersion:"+preferencePreyVersion);
            boolean missing=PreyConfig.getPreyConfig(this).isMissing();
            if (PreyVersion.equals(preferencePreyVersion)) {
                PreyConfig.getPreyConfig(this).setPreferencePreyVersion(PreyVersion);
                PreyWebServices.getInstance().sendEvent(this, PreyConfig.ANDROID_VERSION_UPDATED);
            }
            if (deviceKey != null && deviceKey != "") {
                PreyConfig.getPreyConfig(this).registerC2dm();
                new Thread() {
                    public void run() {
                        GeofenceController.getInstance().init(getApplicationContext());
                    }
                }.start();
                new Thread() {
                    public void run() {
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
                        AwareConfig.getAwareConfig(getApplicationContext()).init();
                    }
                }.start();
                new Thread() {
                    public void run() {
                        if (PreyConfig.getPreyConfig(getApplicationContext()).isSimChanged()) {
                            JSONObject info = new JSONObject();
                            try {
                                String lineNumber=PreyTelephonyManager.getInstance(getApplicationContext()).getLine1Number();
                                if(lineNumber!=null&&!"".equals(lineNumber)) {
                                    info.put("new_phone_number", PreyTelephonyManager.getInstance(getApplicationContext()).getLine1Number());
                                }
                                info.put("sim_serial_number", PreyConfig.getPreyConfig(getApplicationContext()).getSimSerialNumber());
                            } catch (Exception e) {
                            }
                            Event event= new Event(Event.SIM_CHANGED, info.toString());
                            new EventManagerRunner(getApplicationContext(), event) ;
                        }
                    }
                }.start();
            }
        } catch (Exception e) {
            PreyLogger.e("Error PreyApp:" + e.getMessage(), e);
        }
    }

}
