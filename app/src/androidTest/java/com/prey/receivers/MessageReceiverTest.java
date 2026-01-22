/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.events.Event;
import com.prey.security.MessageProcessor;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test suite for the {@link MessageReceiver}.
 * <p>
 * This class verifies that the {@link MessageReceiver} correctly processes incoming broadcast
 * messages and triggers the appropriate events within the application. It simulates sending
 * broadcast intents with different actions and asserts that the corresponding event is recorded
 * in {@link PreyConfig}.
 */
public class MessageReceiverTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setApiKey("ApiKey");
        preyConfig.setDeviceId("DeviceId");
    }

    /**
     * Tests that receiving a {@link MessageProcessor#ACTION_WAKE_UP} action
     * correctly sets the last event to {@link Event#ANDROID_MATCH}. This verifies
     * that the system responds to a wake-up signal by logging the appropriate
     * matching event, which is a key part of the device check-in process.
     */
    @Test
    public void sendingWakeUpAction_ShouldSetAndroidMatchEvent() {
        assertLastEventAfterAction(MessageProcessor.ACTION_WAKE_UP, Event.ANDROID_MATCH);
    }

    /**
     * Verifies that when a {@link MessageProcessor#ACTION_MATCH} message is received,
     * the last recorded event in {@link PreyConfig} is set to {@link Event#ANDROID_MATCH_UP}.
     * This test simulates the reception of a "match" action and asserts that the application state
     * is updated correctly to reflect this event.
     */
    @Test
    public void sendingMatchAction_ShouldSetAndroidMatchUpEvent() {
        assertLastEventAfterAction(MessageProcessor.ACTION_MATCH, Event.ANDROID_MATCH_UP);
    }

    /**
     * Tests that when a {@link MessageProcessor#ACTION_DEVICE_ADMIN} action is received,
     * the last event is correctly set to {@link Event#ANDROID_DEVICE_ADMIN}.
     * This verifies the receiver's ability to process a device admin activation message and update the application's state accordingly.
     */
    @Test
    public void sendingDeviceAdminAction_ShouldSetDeviceAdminEvent() {
        assertLastEventAfterAction(MessageProcessor.ACTION_DEVICE_ADMIN, Event.ANDROID_DEVICE_ADMIN);
    }

    /**
     * Tests that when a {@code ACTION_REVOKED_DEVICE_ADMIN} message is received,
     * the last event is correctly set to {@code ANDROID_REVOKED_DEVICE_ADMIN}.
     * This verifies the receiver's ability to process the device admin revocation
     * action and update the application's state accordingly.
     */
    @Test
    public void sendingRevokedDeviceAdminAction_ShouldSetRevokedDeviceAdminEvent() {
        assertLastEventAfterAction(MessageProcessor.ACTION_REVOKED_DEVICE_ADMIN, Event.ANDROID_REVOKED_DEVICE_ADMIN);
    }

    /**
     * Helper method to assert the last recorded event after a specific action is triggered.
     * It sends a broadcast message with the given action twice to simulate potential real-world scenarios,
     * waits for a short period to allow the receiver to process the message, and then verifies
     * that the last event stored in {@link PreyConfig} matches the expected event.
     *
     * @param action        The action string to be sent in the broadcast message (e.g., {@link MessageProcessor#ACTION_WAKE_UP}).
     * @param expectedEvent The expected event string that should be recorded as a result of the action (e.g., {@link Event#ANDROID_MATCH}).
     */
    private void assertLastEventAfterAction(String action, String expectedEvent) {
        sendMessage(action, null);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String lastEvent = preyConfig.getLastEvent();
        assertEquals(expectedEvent, lastEvent);
    }

    /**
     * Constructs and sends a broadcast {@link Intent} to simulate receiving a message.
     * This helper method is used in tests to trigger the {@link MessageReceiver}.
     * It builds a JSON payload containing the specified action, API key, device key,
     * and an optional error message. The JSON is then put into an Intent with the
     * action "com.prey.ACTION_MESSAGE" and broadcasted within the application's context.
     *
     * @param action The specific action to be included in the message payload (e.g., "wake_up").
     * @param error  An optional error string to include in the message payload. Can be null.
     */
    public void sendMessage(String action, String error) {
        PreyLogger.i("--->ACTION_MESSAGE response");
        JSONObject json = new JSONObject();
        try {
            json.put("action", action);
            json.put("apiKey", preyConfig.getApiKey());
            json.put("deviceKey", preyConfig.getDeviceId());
            if (error != null) {
                json.put("error", error);
            }
        } catch (JSONException e) {
            PreyLogger.e(String.format("Error creating JSON for broadcast:%s", e.getMessage()), e);
            fail(String.format("Failed to create JSON message:%s", e.getMessage()));
            return;
        }
        Intent intent = new Intent("com.prey.ACTION_MESSAGE");
        intent.setPackage("com.prey");
        intent.putExtra("message", json.toString());
        PreyLogger.i(String.format("--> Sending broadcast for ACTION_MESSAGE:%s", action));
        context.sendBroadcast(intent);
    }

}