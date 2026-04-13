/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Context;
import android.content.RestrictionsManager;
import android.os.Bundle;

import com.prey.PreyConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Robolectric test suite for the MDM detection logic in {@link LoginActivity}.
 * <p>
 * Tests that LoginActivity correctly routes to SplashMdmActivity when MDM
 * restrictions with a setup_key are present and the device is not registered.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class LoginActivityRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
    }

    @Test
    public void givenNoMdmRestrictions_whenStartup_thenDoesNotNavigateToMdmSplash() {
        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        android.content.Intent nextIntent = shadow.getNextStartedActivity();

        assertNotNull("Should navigate somewhere", nextIntent);
        String targetClass = nextIntent.getComponent().getClassName();
        assertTrue(
                "Should not navigate to SplashMdmActivity when no MDM restrictions",
                !targetClass.contains("SplashMdmActivity")
        );
    }

    @Test
    public void givenDeviceAlreadyRegistered_whenMdmRestrictionsPresent_thenDoesNotNavigateToMdmSplash() {
        // Simulate a registered device
        preyConfig.setDeviceId("existing-device-id");
        preyConfig.setApiKey("existing-api-key");

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        android.content.Intent nextIntent = shadow.getNextStartedActivity();

        assertNotNull("Should navigate somewhere", nextIntent);
        String targetClass = nextIntent.getComponent().getClassName();
        assertTrue(
                "Should not navigate to SplashMdmActivity when device already registered",
                !targetClass.contains("SplashMdmActivity")
        );

        // Clean up
        preyConfig.setDeviceId("");
        preyConfig.setApiKey("");
    }
}
