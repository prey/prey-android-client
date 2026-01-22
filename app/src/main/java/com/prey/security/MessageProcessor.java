/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.security;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageProcessor {

    public static final String ACTION_WAKE_UP = "wake_up";
    public static final String ACTION_MATCH = "match";
    public static final String ACTION_MATCH_UP = "match_up";
    public static final String ACTION_DEVICE_ADMIN = "device_admin";
    public static final String ACTION_REVOKED_DEVICE_ADMIN = "revoked_device_admin";
    public static final String ACTION_FACTORY_RESET = "factory_reset";
    public static final String ACTION_FACTORY_RESET_ERROR = "factory_reset_error";

    private static final MessageProcessor instance = new MessageProcessor();

    private MessageProcessor() {
        // Private constructor to prevent instantiation
    }

    // No need for null-checking. The instance is guaranteed to exist.
    public static MessageProcessor getInstance() {
        return instance;
    }

    public void receive(Context context, JSONObject json) {
        PreyLogger.d("<---ACTION_MESSAGE");
        try {
            String apiKey = json.optString("apiKey");
            PreyLogger.d(String.format("apiKey:%s", apiKey));
            String deviceKey = json.optString("deviceKey");
            PreyLogger.d(String.format("deviceKey:%s", deviceKey));
            String action = json.getString("action");
            PreyLogger.d(String.format("action:%s", action));
            PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
            switch (action) {

                //I wake up and begin the union.
                case ACTION_WAKE_UP -> {
                    //The last event is added
                    preyConfig.setLastEvent(Event.ANDROID_MATCH);
                    send(context, ACTION_MATCH);
                }

                //I receive data and notify the panel.
                case ACTION_MATCH -> {
                    startEvent(context, Event.ANDROID_MATCH_UP);
                    //The last event is added
                    preyConfig.setLastEvent(Event.ANDROID_MATCH_UP);
                    send(context, ACTION_MATCH_UP);
                }

                //I receive the enabled permission from the administrative permit of the extension and notify the panel.
                case ACTION_DEVICE_ADMIN -> {
                    startEvent(context, Event.ANDROID_DEVICE_ADMIN);
                    //The last event is added
                    preyConfig.setLastEvent(ACTION_DEVICE_ADMIN);
                    send(context, Event.ANDROID_DEVICE_ADMIN);
                    preyConfig.setDeviceAdminExtension(true);
                }

                //I receive the disabled permission from the administrative permit of the extension and notify the panel.
                case ACTION_REVOKED_DEVICE_ADMIN -> {
                    startEvent(context, Event.ANDROID_REVOKED_DEVICE_ADMIN);
                    //The last event is added
                    preyConfig.setLastEvent(ACTION_REVOKED_DEVICE_ADMIN);
                    send(context, Event.ANDROID_REVOKED_DEVICE_ADMIN);
                    preyConfig.setDeviceAdminExtension(false);
                }

                //I received that I initiated the factory reset and notify the panel.
                case ACTION_FACTORY_RESET -> {
                    String messageId = "";
                    //The last event is added
                    preyConfig.setLastEvent(Event.ANDROID_FACTORY_RESET);
                    startEvent(context, Event.ANDROID_FACTORY_RESET);
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                            context, "processed",
                            messageId, UtilJson.makeMapParam("start", "wipe", "stopped", null)
                    );
                }

                //I received the factory reset error and notify the panel.
                case ACTION_FACTORY_RESET_ERROR -> {
                    String messageId = "";
                    String errorString = json.getString("error");
                    //The last event is added
                    preyConfig.setLastEvent(Event.ANDROID_FACTORY_RESET_ERROR);
                    startEvent(context, Event.ANDROID_FACTORY_RESET_ERROR, errorString);
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                            context, "failed",
                            messageId, UtilJson.makeMapParam("start", "wipe", "failed", errorString)
                    );
                }
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
    }

    void startEvent(Context context, String nameEvent) {
        startEvent(context, nameEvent, null);
    }

    void startEvent(Context context, String nameEvent, String errorString) {
        Event event = new Event(nameEvent);
        if (!TextUtils.isEmpty(errorString)) {
            event.setInfo(errorString);
        }
        new Thread(new EventManagerRunner(context, event)).start();
    }

    public void send(Context context, String action) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        if (preyConfig.isThisDeviceAlreadyRegisteredWithPrey()) {
            PreyLogger.i(String.format("--->ACTION_MESSAGE action:%s", action));
            JSONObject jsonString = new JSONObject();
            try {
                jsonString.put("apiKey", preyConfig.getApiKey());
                jsonString.put("deviceKey", preyConfig.getDeviceId());
                jsonString.put("deviceName", preyConfig.getDeviceName());
                jsonString.put("action", action);
            } catch (JSONException e) {
                PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
                return; // Exit early if JSON creation fails
            }
            PreyLogger.i(String.format("--->ACTION_MESSAGE json:%s", jsonString));
            Intent intent = new Intent("com.prey.security.ACTION_MESSAGE");
            intent.setPackage("com.prey.security");
            intent.putExtra("message", jsonString.toString());
            context.sendBroadcast(intent);
        }
    }

}
