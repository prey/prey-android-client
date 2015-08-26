package com.prey.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.activities.WelcomeActivity;

import com.prey.R;
import com.prey.beta.actions.PreyBetaController;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by oso on 21-08-15.
 */
public class PreyGcmListenerService extends GcmListenerService {



    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        PreyLogger.d("PUSH_______________");
        Context ctx=getApplicationContext();
        PreyConfig config=PreyConfig.getPreyConfig(ctx);

        Set<String> set=data.keySet();
        Iterator<String> ite=set.iterator();
        while(ite.hasNext()){
            String key=ite.next();
            PreyLogger.d("___["+key+"]"+data.getString(key));
        }

        String body = data.getString("body");
        String version = data.getString("version");

        String api_key=data.getString("api_key");
        String remote_email=data.getString("remote_email");

        String cmd=data.getString("cmd");

        if((api_key!=null&&!"".equals(api_key))||(body!=null&&body.indexOf("api_key")>0)){
            registrationPlanB(ctx,api_key, remote_email, body);
        }else{
            handleMessageBeta(ctx, body, version, cmd);

        }
    }
    // [END receive_message]
    private void registrationPlanB(Context context, String apiKey, String remoteEmail,String pushedMessage){


    }

    private void handleMessageBeta(Context context, String body,String version,String cmd) {
        PreyLogger.d("Push notification received, waking up Prey right now!");
        PreyLogger.i("Push message received " + body+ " version:"+version);
        PreyBetaController.startPrey(context, cmd);
    }

}
