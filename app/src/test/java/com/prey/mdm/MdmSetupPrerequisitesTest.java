/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.mdm;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.prey.PreyConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class MdmSetupPrerequisitesTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setNotificationId("");
    }

    @Test
    public void waitUntilReady_returnsTrueWhenPushTokenAlreadyConfirmed() {
        MdmSetupPrerequisites prerequisites = new MdmSetupPrerequisites(
                millis -> {
                },
                () -> 0L
        );
        preyConfig.setNotificationId("push-token");

        boolean ready = prerequisites.waitUntilReady(context, 1L);

        assertTrue(ready);
    }

    @Test
    public void waitUntilReady_returnsFalseWhenPushTokenWasNotConfirmed() {
        MdmSetupPrerequisites prerequisites = new MdmSetupPrerequisites(
                millis -> {
                },
                new IncrementingClock()
        );

        boolean ready = prerequisites.waitUntilReady(context, 1000L);

        assertFalse(ready);
    }

    private static class IncrementingClock implements MdmSetupPrerequisites.Clock {
        private long now;

        @Override
        public long now() {
            now += 1000L;
            return now;
        }
    }
}
