/*******************************************************************************
 * Created by Prey
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.prey.actions.location.LocationUpdatesService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class ServiceStartCompatTest {

    @Test
    @Config(sdk = 35)
    public void givenAndroidOOrAbove_whenStartingServiceCompat_thenUsesForegroundStart() {
        RecordingContext context = new RecordingContext(ApplicationProvider.getApplicationContext());
        Intent intent = new Intent(context, LocationUpdatesService.class);

        ServiceStartCompat.startServiceCompat(context, intent);

        assertNotNull(context.foregroundIntent);
        assertEquals(LocationUpdatesService.class.getName(), context.foregroundIntent.getComponent().getClassName());
        assertNull(context.backgroundIntent);
    }

    @Test
    @Config(sdk = 25)
    public void givenPreAndroidO_whenStartingServiceCompat_thenUsesBackgroundStart() {
        RecordingContext context = new RecordingContext(ApplicationProvider.getApplicationContext());
        Intent intent = new Intent(context, LocationUpdatesService.class);

        ServiceStartCompat.startServiceCompat(context, intent);

        assertNotNull(context.backgroundIntent);
        assertEquals(LocationUpdatesService.class.getName(), context.backgroundIntent.getComponent().getClassName());
        assertNull(context.foregroundIntent);
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
