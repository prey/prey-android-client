/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.prey.PreyConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

/**
 * Tests for {@link PreyBetaController#startPrey}.
 *
 * <p>The activity originally went through {@code startService(PreyBetaRunnerService)}
 * to spin up the actions runner. On Android 12+ that call throws
 * {@code BackgroundServiceStartNotAllowedException} whenever this entry point
 * fires from a non-foreground context (FCM, boot receiver, app onCreate while
 * the app isn't visible). The fix invokes {@code PreyBetaActionsRunnner}
 * directly. These tests pin that no startService call is attempted, so the
 * background-restriction path can never reappear silently after a refactor.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class PreyBetaControllerRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
    }

    @After
    public void tearDown() {
        // Reset registration state so other tests don't see leakage.
        preyConfig.setApiKey("");
        preyConfig.setDeviceId("");
    }

    @Test
    public void startPrey_withRegisteredDevice_doesNotCallStartService() {
        // Make isThisDeviceAlreadyRegisteredWithPrey() return true.
        preyConfig.setApiKey("test-api-key");
        preyConfig.setDeviceId("test-device-id");

        ShadowApplication shadowApp = shadowOf((Application) context);
        drainStartedServices(shadowApp);

        PreyBetaController.startPrey(context);

        Intent next = shadowApp.getNextStartedService();
        assertNull(
                "startPrey must not call startService — that path raised "
                        + "BackgroundServiceStartNotAllowedException on Android 12+ "
                        + "and was a redundant proxy to the actions runner anyway",
                next
        );
    }

    @Test
    public void startPrey_withRegisteredDevice_setsRunFlag() {
        // The runner thread is what actually drives instructions; here we just
        // assert the synchronous side-effect that gates whether the rest of
        // the app considers Prey "running".
        preyConfig.setApiKey("test-api-key");
        preyConfig.setDeviceId("test-device-id");
        preyConfig.setRun(false);

        PreyBetaController.startPrey(context);

        assertTrue(
                "startPrey must flip the run flag so other components observe Prey as active",
                preyConfig.isRun()
        );
    }

    @Test
    public void startPrey_withUnregisteredDevice_isANoOp() {
        // Empty apiKey and deviceId → isThisDeviceAlreadyRegisteredWithPrey() == false.
        preyConfig.setApiKey("");
        preyConfig.setDeviceId("");
        preyConfig.setRun(false);

        ShadowApplication shadowApp = shadowOf((Application) context);
        drainStartedServices(shadowApp);

        PreyBetaController.startPrey(context, "fake-cmd");

        assertNull(
                "An unregistered device must not trigger any service / runner work",
                shadowApp.getNextStartedService()
        );
        assertTrue(
                "An unregistered device must not flip the run flag",
                !preyConfig.isRun()
        );
    }

    private static void drainStartedServices(ShadowApplication shadowApp) {
        while (shadowApp.getNextStartedService() != null) {
            // drain queue from setup work
        }
    }
}
