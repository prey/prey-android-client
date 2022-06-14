/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2022 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.prey.events.factories.EventFactory;

public class PreyCloseNotificationService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    /**
     * Service that closes the notification
     *
     * @param intent
     * @param startId
     */
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(EventFactory.NOTIFICATION_ID);
    }

}
