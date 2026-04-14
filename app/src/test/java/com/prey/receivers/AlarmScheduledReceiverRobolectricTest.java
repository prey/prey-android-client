/*******************************************************************************
 * Created by Prey
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.prey.beta.services.PreyBetaRunnerService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class AlarmScheduledReceiverRobolectricTest {

    @Test
    @Config(sdk = 35)
    public void givenModernAndroid_whenAlarmReceived_thenStartsForegroundService() {
        RecordingContext context = new RecordingContext(ApplicationProvider.getApplicationContext());

        new AlarmScheduledReceiver().onReceive(context, new Intent("test"));

        assertNotNull(context.foregroundIntent);
        assertEquals(PreyBetaRunnerService.class.getName(), context.foregroundIntent.getComponent().getClassName());
        assertNull(context.backgroundIntent);
    }

    private static final class RecordingContext extends ContextWrapper {

        private Intent foregroundIntent;
        private Intent backgroundIntent;

        private RecordingContext(Context base) {
            super(base);
        }

        @Override
        public android.content.ComponentName startForegroundService(Intent service) {
            foregroundIntent = service;
            return service.getComponent();
        }

        @Override
        public android.content.ComponentName startService(Intent service) {
            backgroundIntent = service;
            return service.getComponent();
        }
    }
}
