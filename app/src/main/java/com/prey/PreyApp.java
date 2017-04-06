/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.Application;
import android.content.Intent;

import com.prey.actions.fileretrieval.FileretrievalController;
import com.prey.actions.fileretrieval.FileretrievalService;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.report.ReportScheduled;
import com.prey.net.PreyWebServices;
import com.prey.net.offline.OfflineController;

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
                new Thread() {
                    public void run() {
                        OfflineController.getInstance().run(getApplicationContext());
                    }
                }.start();
                if (PreyConfig.getPreyConfig(this).isMissing()) {
                    if (PreyConfig.getPreyConfig(this).getIntervalReport() != null && !"".equals(PreyConfig.getPreyConfig(this).getIntervalReport())) {
                        ReportScheduled.getInstance(this).run();
                    }
                }
            }
        } catch (Exception e) {
            PreyLogger.e("Error PreyApp:" + e.getMessage(), e);
        }
    }


}
