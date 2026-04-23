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
        preyConfig.setMdmSetupLocationSent(false);
    }

    @Test
    public void waitUntilReady_returnsTrueWhenPushTokenAlreadyConfirmedAndLocationUploadSucceeds() {
        FakeLocationUploader uploader = new FakeLocationUploader(true);
        MdmSetupPrerequisites prerequisites = new MdmSetupPrerequisites(
                uploader,
                millis -> {
                },
                () -> 0L
        );
        preyConfig.setNotificationId("push-token");

        boolean ready = prerequisites.waitUntilReady(context, 1L);

        assertTrue(ready);
        assertTrue(preyConfig.isMdmSetupLocationSent());
        assertTrue(uploader.invoked);
    }

    @Test
    public void waitUntilReady_returnsFalseWhenPushTokenWasNotConfirmed() {
        FakeLocationUploader uploader = new FakeLocationUploader(true);
        MdmSetupPrerequisites prerequisites = new MdmSetupPrerequisites(
                uploader,
                millis -> {
                },
                new IncrementingClock()
        );

        boolean ready = prerequisites.waitUntilReady(context, 1000L);

        assertFalse(ready);
        assertTrue(preyConfig.isMdmSetupLocationSent());
        assertTrue(uploader.invoked);
    }

    private static class FakeLocationUploader implements MdmSetupPrerequisites.LocationUploader {
        private final boolean result;
        private boolean invoked;

        private FakeLocationUploader(boolean result) {
            this.result = result;
        }

        @Override
        public boolean upload(Context context) {
            invoked = true;
            if (result) {
                PreyConfig.getPreyConfig(context).setMdmSetupLocationSent(true);
            }
            return result;
        }
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
