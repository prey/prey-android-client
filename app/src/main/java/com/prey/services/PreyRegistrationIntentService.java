/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.support.v4.content.LocalBroadcastManager;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

public class PreyRegistrationIntentService extends IntentService {

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public PreyRegistrationIntentService() {
        super(PreyConfig.TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            PreyLogger.i("[START register_for_gcm]");
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(FileConfigReader.getInstance(this).getGcmId(),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            PreyLogger.i("GCM Registration Token: " + token);

            sendRegistrationToServer(token);

        } catch (Exception e) {
            PreyLogger.e("Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(String token) {
        new UpdateCD2MId().execute(token, getApplicationContext());
    }

    private class UpdateCD2MId extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... data) {
            try {
                Context ctx = (Context) data[1];
                String registration = FileConfigReader.getInstance(ctx).getGcmIdPrefix() + (String) data[0];
                PreyHttpResponse response = PreyWebServices.getInstance().setPushRegistrationId(ctx, registration);
                PreyConfig.getPreyConfig(ctx).setNotificationId(registration);
                if (response != null) {
                    PreyLogger.d("response:" + response.toString());
                }
                PreyConfig.getPreyConfig(ctx).setRegisterC2dm(true);
                // PreyBetaController.startPrey(ctx);
            } catch (Exception e) {
                PreyLogger.e("Failed registering to CD2M: " + e.getLocalizedMessage(), e);
            }
            return null;
        }
    }
}


