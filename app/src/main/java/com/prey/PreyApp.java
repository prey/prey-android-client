/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import com.prey.actions.fileretrieval.FileretrievalController;
import com.prey.actions.fileretrieval.FileretrievalService;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.report.ReportScheduled;
import com.prey.activities.LoginActivity;
import com.prey.net.PreyWebServices;
import com.prey.net.offline.OfflineController;

import java.text.SimpleDateFormat;
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
                new Thread() {
                    public void run() {
                        OfflineController.getInstance().run(getApplicationContext());
                    }
                }.start();
                if (missing) {
                    if (PreyConfig.getPreyConfig(this).getIntervalReport() != null && !"".equals(PreyConfig.getPreyConfig(this).getIntervalReport())) {
                        ReportScheduled.getInstance(this).run();
                    }
                }

                SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
                String notificationAndroid7New=sdf.format(new Date());
                String notificationAndroid7Old= PreyConfig.getPreyConfig(this).getNotificationAndroid7();
                PreyLogger.d("notificationAndroid7New:"+notificationAndroid7New+" notificationAndroid7Old:"+notificationAndroid7Old);
                PreyLogger.d("PreyPermission.canDrawOverlays(this):"+PreyPermission.canDrawOverlays(this));
                //isNougatOrAbove
                if(!missing&&PreyConfig.getPreyConfig(this).isMarshmallowOrAbove() && !PreyPermission.canDrawOverlays(this)&&!notificationAndroid7New.equals(notificationAndroid7Old)){
                    int STATUS_ICON_REQUEST_CODE=1;

                    PreyConfig.getPreyConfig(this).setNotificationAndroid7(notificationAndroid7New);
                    android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setContentTitle(this.getString(R.string.warning_android7_notification_title))
                            .setContentText(getResources().getString(R.string.warning_android7_notification_body))
                            .setSmallIcon(R.drawable.logo);

                    Intent intent = new Intent(this, LoginActivity.class);
                    PendingIntent pIntent = PendingIntent.getActivity(this, STATUS_ICON_REQUEST_CODE, intent, 0);
                    builder.setContentIntent(pIntent);
                    NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

                    Notification notif = builder.build();

                    notif.flags |= Notification.FLAG_AUTO_CANCEL;
                    mNotificationManager.notify(STATUS_ICON_REQUEST_CODE, notif);

                }
            }
        } catch (Exception e) {
            PreyLogger.e("Error PreyApp:" + e.getMessage(), e);
        }
    }

}
