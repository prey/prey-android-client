/*******************************************************************************
 * Created by OpenAI Codex
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.prey.activities.PopUpAlertActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class AlertThreadRobolectricTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void givenFullscreenAlert_whenTriggered_thenPostsNotificationInsteadOfLaunchingActivity() {
        AlertThread alertThread = new AlertThread(context, "Device alert", "message-id", "job-id", true);
        Intent popupIntent = AlertThread.buildPopupIntent(context, "Device alert", 101);

        alertThread.fullscreen(101);

        ShadowApplication shadowApplication = Shadows.shadowOf((android.app.Application) context);
        Intent nextIntent = shadowApplication.getNextStartedActivity();

        assertNull(nextIntent);
        assertNotNull(popupIntent.getComponent());
        assertEquals(PopUpAlertActivity.class.getName(), popupIntent.getComponent().getClassName());
        assertEquals("Device alert", popupIntent.getStringExtra("alert_message"));
        assertFalse(popupIntent.hasCategory(Intent.CATEGORY_LAUNCHER));
    }
}
