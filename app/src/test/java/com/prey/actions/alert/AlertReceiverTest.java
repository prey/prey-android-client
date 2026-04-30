/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.prey.net.PreyWebServices;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for {@link AlertReceiver}.
 *
 * <p>The receiver fires the {@code action_stopped} HTTP notify when the user
 * taps "close" on the alert notification. Production previously spawned a
 * fire-and-forget worker thread for the POST and returned from
 * {@code onReceive} immediately, which dropped the process to "cached"
 * state — Android (especially OEMs like Oppo) could then kill the process
 * before the POST completed, silently losing the event server-side.
 *
 * <p>The fix uses {@code BroadcastReceiver.goAsync()} to keep the receiver
 * (and therefore the process) alive until {@code result.finish()} is called
 * inside the worker's {@code finally}. These tests pin the contract:
 *
 * <ul>
 *   <li>The receiver routes the notify through the synchronous variant
 *       (not the default async one that would spawn its own thread).</li>
 *   <li>The notify carries {@code start/alert/stopped} with the correct
 *       correlation id and reason from the notification's PendingIntent.</li>
 * </ul>
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class AlertReceiverTest {

    private RecordingPreyWebServices recording;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        recording = new RecordingPreyWebServices();
        PreyWebServices.setInstanceForTests(recording);
    }

    @After
    public void tearDown() {
        PreyWebServices.resetInstanceForTests();
    }

    @Test
    public void onReceive_postsStoppedNotifyWithCorrectStatusAndCorrelationId() throws Exception {
        Intent intent = new Intent("12345");
        intent.putExtra("notificationId", 12345);
        intent.putExtra("messageId", "msg-abc");
        intent.putExtra("reason", "{\"device_job_id\":\"job-xyz\"}");

        new AlertReceiver().onReceive(context, intent);

        assertTrue(
                "Worker thread must complete the sync notify within the goAsync window",
                recording.syncCallReceived.await(5, TimeUnit.SECONDS)
        );
        assertEquals(
                "Receiver must use the synchronous notify variant — the async one "
                        + "spawns its own thread and defeats the goAsync protection",
                1,
                recording.syncCalls.size()
        );
        SyncCall call = recording.syncCalls.get(0);
        assertEquals("processed", call.status);
        assertEquals(
                "messageId from the notification's PendingIntent must travel through "
                        + "as the correlationId so the panel can correlate the stop event",
                "msg-abc",
                call.correlationId
        );
        assertEquals("start", call.params.get("command"));
        assertEquals("alert", call.params.get("target"));
        assertEquals(
                "Status must be 'stopped' — that's the whole reason this receiver exists",
                "stopped",
                call.params.get("status")
        );
        assertEquals(
                "Reason must carry the device_job_id from the original intent",
                "{\"device_job_id\":\"job-xyz\"}",
                call.params.get("reason")
        );
    }

    @Test
    public void onReceive_doesNotUseAsyncNotifyVariant() throws Exception {
        // Defensive pin: if a future refactor switches back to the async
        // variant we want this test to fail loudly. Without goAsync + sync
        // POST the production bug returns silently.
        Intent intent = new Intent("99");
        intent.putExtra("notificationId", 99);
        intent.putExtra("messageId", "m");
        intent.putExtra("reason", "");

        new AlertReceiver().onReceive(context, intent);

        assertTrue(recording.syncCallReceived.await(5, TimeUnit.SECONDS));
        assertEquals(
                "AlertReceiver must NOT call the async notify variant — that's "
                        + "the legacy behavior that loses POSTs on cached-state kill",
                0,
                recording.asyncCalls.size()
        );
    }

    // =========================================================================
    // Recording stub
    // =========================================================================

    private static class RecordingPreyWebServices extends PreyWebServices {
        final java.util.List<SyncCall> syncCalls = new java.util.ArrayList<>();
        final java.util.List<Map<String, String>> asyncCalls = new java.util.ArrayList<>();
        final CountDownLatch syncCallReceived = new CountDownLatch(1);

        @Override
        public void sendNotifyActionResultPreyHttpSync(
                Context ctx, String status, String correlationId, Map<String, String> params) {
            syncCalls.add(new SyncCall(status, correlationId, new HashMap<>(params)));
            syncCallReceived.countDown();
        }

        @Override
        public void sendNotifyActionResultPreyHttp(
                Context ctx, String status, String correlationId, Map<String, String> params) {
            // If anyone hits this overload from AlertReceiver, the test for
            // "doesNotUseAsyncNotifyVariant" will catch it.
            asyncCalls.add(new HashMap<>(params));
        }

        // The remaining overloads aren't expected to be called from AlertReceiver
        // but we override them defensively so a future change can't sneak past.
        @Override
        public String sendNotifyActionResultPreyHttp(Context ctx, Map<String, String> params) {
            asyncCalls.add(new HashMap<>(params));
            return null;
        }

        @Override
        public int uploadLog(Context ctx, File file) {
            return 0;
        }
    }

    private static class SyncCall {
        final String status;
        final String correlationId;
        final Map<String, String> params;

        SyncCall(String status, String correlationId, Map<String, String> params) {
            this.status = status;
            this.correlationId = correlationId;
            this.params = params;
        }
    }
}
