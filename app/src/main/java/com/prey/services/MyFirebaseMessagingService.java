/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.prey.PreyLogger;
import com.prey.beta.actions.PreyBetaController;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        PreyLogger.d("FIREBASE onMessageReceived" );
        if (remoteMessage.getData().size() > 0) {
            final String text= remoteMessage.getData().toString();
            PreyLogger.d("FIREBASE data:"+text);
            String cmd=null;
            try {
                cmd = remoteMessage.getData().get("cmd");
            } catch (Exception e) {
                cmd = null;
            }
            PreyLogger.d("FIREBASE cmd:"+cmd);
            PreyBetaController.startPrey(this,cmd);
        }
    }

}