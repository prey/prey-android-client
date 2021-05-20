/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.beta.actions.PreyBetaController;

import java.util.Iterator;
import java.util.Set;

public class PreyGcmListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        PreyLogger.d("PUSH_______________");
        Context ctx = getApplicationContext();
        PreyConfig config = PreyConfig.getPreyConfig(ctx);
        Set<String> set = data.keySet();
        Iterator<String> ite = set.iterator();
        while (ite.hasNext()) {
            String key = ite.next();
            PreyLogger.d("___[" + key + "]" + data.getString(key));
        }
        String body = data.getString("body");
        String version = data.getString("version");
        String api_key = data.getString("api_key");
        String remote_email = data.getString("remote_email");
        String cmd = data.getString("cmd");
        if ((api_key != null && !"".equals(api_key)) || (body != null && body.indexOf("api_key") > 0)) {
            registrationPlanB(ctx, api_key, remote_email, body);
        } else {
            handleMessageBeta(ctx, body, version, cmd);
        }
    }

    private void registrationPlanB(Context context, String apiKey, String remoteEmail, String pushedMessage) {
    }

    private void handleMessageBeta(Context context, String body, String version, String cmd) {
        PreyLogger.d("Push notification received, waking up Prey right now!");
        PreyLogger.d("Push message received " + body + " version:" + version);
        PreyBetaController.startPrey(context, cmd);
    }

}