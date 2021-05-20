/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.beta.actions.PreyBetaController;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

public class C2DMReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
            handleRegistration(context, intent);
        } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            handleMessage(context, intent);
        }
    }

    private void handleMessage(Context context, Intent intent) {
        PreyLogger.d("PUSH_______________");
        PreyConfig config=PreyConfig.getPreyConfig(context);
        Set<String> set=intent.getExtras().keySet();
        Iterator<String> ite=set.iterator();
        while(ite.hasNext()) {
            String key = ite.next();
            PreyLogger.d("___[" + key + "]" + intent.getExtras().get(key));
        }
        String cmd=null;
        try {
            if(intent.getExtras().containsKey("cmd")) {
                String gcmCmd = intent.getExtras().getString("cmd");
                if (gcmCmd != null & gcmCmd.length() > 0) {
                    gcmCmd = "{" + gcmCmd + "}";
                    JSONObject jsnobject = new JSONObject(gcmCmd);
                    if (jsnobject != null) {
                        JSONArray jsonArray = jsnobject.getJSONArray("instruction");
                        cmd = jsonArray.get(0).toString();
                        PreyLogger.d("___cmd:" + cmd);
                    }
                }
            }
        } catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        PreyBetaController.startPrey(context,cmd);
    }

    private void handleRegistration(Context context, Intent intent) {
        String registration = intent.getStringExtra("registration_id");
        if (intent.getStringExtra("error") != null) {
            PreyLogger.d("Couldn't register to c2dm: " + intent.getStringExtra("error"));
            PreyConfig.getPreyConfig(context).setRegisterC2dm(false);
            PreyConfig.getPreyConfig(context).setNotificationId("");
        } else if (intent.getStringExtra("unregistered") != null) {
            // unregistration done, new messages from the authorized sender will
            // be rejected
            PreyLogger.d("Unregistered from c2dm: " + intent.getStringExtra("unregistered"));
            PreyConfig.getPreyConfig(context).setRegisterC2dm(false);
            PreyConfig.getPreyConfig(context).setNotificationId("");
        } else if (registration != null) {
            PreyLogger.d("Registration id: " + registration);
            if(PreyConfig.getPreyConfig(context).getDeviceId()!=null&&!"".equals(PreyConfig.getPreyConfig(context).getDeviceId())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    new UpdateCD2MId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, registration, context);
                else
                    new UpdateCD2MId().execute(registration, context);
            }
        }
    }

    private class UpdateCD2MId extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... data) {
            try {
                Context ctx=(Context) data[1];
                String registration = FileConfigReader.getInstance(ctx).getGcmIdPrefix() + (String) data[0];
                PreyHttpResponse response=PreyWebServices.getInstance().setPushRegistrationId(ctx, registration);
                PreyConfig.getPreyConfig(ctx).setNotificationId(registration);
                if(response!=null){
                    PreyLogger.d("response:"+response.toString());
                }
                PreyConfig.getPreyConfig(ctx).setRegisterC2dm(true);
                PreyBetaController.startPrey(ctx);
            } catch (Exception e) {
                PreyLogger.e("Failed registering to CD2M: " + e.getLocalizedMessage(), e);
            }
            return null;
        }
    }

}