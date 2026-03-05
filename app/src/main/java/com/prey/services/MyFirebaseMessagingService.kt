/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.prey.PreyLogger
import com.prey.beta.actions.PreyBetaController

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            PreyLogger.d("___ Message data payload: ${remoteMessage.data}")
            // Process the data payload here
            val cmd = remoteMessage.data.get("cmd")
            PreyLogger.d("___ cmd: $cmd")
            PreyBetaController.startPrey(this,cmd);
        }
    }

    override fun onNewToken(token: String) {
        // Called when a new FCM registration token is generated or updated
        PreyLogger.d("___ Refreshed token: $token")
        // If you want to send this token to your app server, do it here
    }

}