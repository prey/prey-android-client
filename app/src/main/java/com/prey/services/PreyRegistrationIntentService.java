package com.prey.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.support.v4.content.LocalBroadcastManager;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

/**
 * Created by oso on 21-08-15.
 */
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
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(FileConfigReader.getInstance(this).getGcmId(),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            PreyLogger.i("GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);



            // [END register_for_gcm]
        } catch (Exception e) {
            PreyLogger.e("Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

        new UpdateCD2MId().execute(token, getApplicationContext());
    }




    private class UpdateCD2MId extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... data) {
            try {
                Context ctx=(Context) data[1];
                String registration = FileConfigReader.getInstance(ctx).getGcmIdPrefix() + (String) data[0];
                PreyHttpResponse response= PreyWebServices.getInstance().setPushRegistrationId(ctx, registration);
                PreyConfig.getPreyConfig(ctx).setNotificationId(registration);
                if(response!=null){
                    PreyLogger.d("response:"+response.toString());
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


