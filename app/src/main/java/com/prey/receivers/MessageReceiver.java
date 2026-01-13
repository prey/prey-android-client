/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.prey.PreyLogger;
import com.prey.security.MessageProcessor;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        PreyLogger.d("----------------->MessageReceiver");
        // Validate the intent action first.
        if (intent == null) {
            return;
        }
        final String messageJson = intent.getStringExtra("message");
        // Check for a valid message payload.
        if (TextUtils.isEmpty(messageJson)) {
            PreyLogger.d("Received message intent with no JSON payload.");
            return;
        }
        final PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject(messageJson);
                // The MessageProcessor should ideally handle its own background work if it's heavy.
                // Assuming it's a quick operation for this example.
                MessageProcessor.getInstance().receive(context, json);
            } catch (JSONException e) {
                PreyLogger.e("Failed to parse message JSON", e);
            } finally {
                // Always finish the async operation.
                pendingResult.finish();
            }
        }).start();
    }

}