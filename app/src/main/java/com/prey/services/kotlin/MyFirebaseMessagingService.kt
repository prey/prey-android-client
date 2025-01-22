/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services.kotlin

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.prey.beta.actions.kotlin.PreyBetaController
import com.prey.kotlin.PreyLogger

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        PreyLogger.d("FIREBASE onMessageReceived")
        if (remoteMessage.data.isNotEmpty()) {
            val text = remoteMessage.data.toString()
            PreyLogger.d("FIREBASE data:$text")
            var cmd: String? = null
            cmd = try {
                remoteMessage.data["cmd"]
            } catch (e: Exception) {
                null
            }
            PreyLogger.d("FIREBASE cmd:$cmd")
            PreyBetaController.getInstance().startPrey(applicationContext!!, cmd)
        }
    }
}