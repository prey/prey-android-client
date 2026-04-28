/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.os.Bundle;

import com.prey.PreyConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowRestrictionsManager;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Robolectric test suite for {@link LoginActivity}.
 * <p>
 * Covers the MDM setup key detection (hasMdmSetupKey) and the provisioning
 * result propagation (onActivityResult) introduced for AMAPI SetupAction support.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class LoginActivityRobolectricTest {
    private static final String EXTRA_LAUNCHED_AS_SETUP_ACTION =
            "com.google.android.apps.work.clouddpc.EXTRA_LAUNCHED_AS_SETUP_ACTION";

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        // Ensure a clean slate: unregistered device, no batch key, no unlock pass.
        preyConfig.setDeviceId("");
        preyConfig.setApiKey("");
        preyConfig.setUnlockPass("");
        preyConfig.setProtectReady(false);
    }

    @After
    public void tearDown() {
        preyConfig.setDeviceId("");
        preyConfig.setApiKey("");
        preyConfig.setUnlockPass("");
        preyConfig.setProtectReady(false);
        setApplicationRestrictions(new Bundle());
    }

    // =========================================================================
    // hasMdmSetupKey — via navigation outcome
    // =========================================================================

    @Test
    public void givenNoMdmRestrictions_whenStartup_thenDoesNotNavigateToMdmSplash() {
        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent nextIntent = shadow.getNextStartedActivity();

        assertNotNull("Should navigate somewhere", nextIntent);
        String targetClass = nextIntent.getComponent().getClassName();
        assertFalse(
                "Should not navigate to SplashMdmActivity when no MDM restrictions",
                targetClass.contains("SplashMdmActivity")
        );
    }

    @Test
    public void givenDeviceAlreadyRegistered_whenMdmRestrictionsPresent_thenDoesNotNavigateToMdmSplash() {
        preyConfig.setDeviceId("existing-device-id");
        preyConfig.setApiKey("existing-api-key");
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "some-setup-key");
        setApplicationRestrictions(restrictions);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent nextIntent = shadow.getNextStartedActivity();

        assertNotNull("Should navigate somewhere", nextIntent);
        String targetClass = nextIntent.getComponent().getClassName();
        assertFalse(
                "Should not navigate to SplashMdmActivity when device already registered",
                targetClass.contains("SplashMdmActivity")
        );
    }

    @Test
    public void givenUnregisteredWithSetupKey_whenStartup_thenNavigatesToMdmSplashForResult() {
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "valid-setup-key");
        setApplicationRestrictions(restrictions);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        ShadowActivity.IntentForResult next = shadow.getNextStartedActivityForResult();

        assertNotNull("Should start SplashMdmActivity for result", next);
        assertTrue(
                "Target should be SplashMdmActivity",
                next.intent.getComponent().getClassName().contains("SplashMdmActivity")
        );
    }

    @Test
    public void givenSetupActionLaunch_whenStartupShowsMdmSplash_thenPropagatesSetupActionExtra() {
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "valid-setup-key");
        setApplicationRestrictions(restrictions);

        Intent setupActionIntent = new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class);
        setupActionIntent.putExtra(EXTRA_LAUNCHED_AS_SETUP_ACTION, true);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class, setupActionIntent);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        ShadowActivity.IntentForResult next = shadow.getNextStartedActivityForResult();

        assertNotNull("Should start SplashMdmActivity for result", next);
        assertTrue(
                "Splash intent should preserve the setup-action hint",
                next.intent.getBooleanExtra(EXTRA_LAUNCHED_AS_SETUP_ACTION, false)
        );
    }

    @Test
    public void givenUnregisteredWithEmptySetupKey_whenStartup_thenDoesNotNavigateToMdmSplash() {
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "");
        setApplicationRestrictions(restrictions);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent nextIntent = shadow.getNextStartedActivity();

        assertNotNull("Should navigate somewhere", nextIntent);
        assertFalse(
                "Should not navigate to SplashMdmActivity when setup_key is empty",
                nextIntent.getComponent().getClassName().contains("SplashMdmActivity")
        );
    }

    @Test
    public void givenUnregisteredWithRestrictionsButNoSetupKey_whenStartup_thenDoesNotNavigateToMdmSplash() {
        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "SN-1234");
        restrictions.putString("device_name", "Test Device");
        setApplicationRestrictions(restrictions);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent nextIntent = shadow.getNextStartedActivity();

        assertNotNull("Should navigate somewhere", nextIntent);
        assertFalse(
                "Should not navigate to SplashMdmActivity without setup_key",
                nextIntent.getComponent().getClassName().contains("SplashMdmActivity")
        );
    }

    @Test
    public void givenProtectReady_whenSetupKeyPresent_thenDoesNotNavigateToMdmSplash() {
        preyConfig.setProtectReady(true);
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "valid-setup-key");
        setApplicationRestrictions(restrictions);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent nextIntent = shadow.getNextStartedActivity();

        assertNotNull("Should navigate somewhere", nextIntent);
        assertFalse(
                "ProtectReady should short-circuit MDM splash routing",
                nextIntent.getComponent().getClassName().contains("SplashMdmActivity")
        );
    }

    // =========================================================================
    // onActivityResult — provisioning result propagation
    // =========================================================================

    private static final int MDM_SETUP_REQUEST = 100;

    @Test
    public void givenMdmSetupRequestOk_whenOnActivityResult_thenFinishesWithResultOk() {
        // Routing through the MDM splash avoids showLogin()'s auto-finish so we can
        // observe the effect of onActivityResult in isolation.
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "valid-setup-key");
        setApplicationRestrictions(restrictions);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();
        ShadowActivity shadow = Shadows.shadowOf(activity);
        assertFalse(
                "Pre-condition: MDM splash route should not auto-finish LoginActivity",
                activity.isFinishing()
        );

        activity.onActivityResult(MDM_SETUP_REQUEST, Activity.RESULT_OK, null);

        assertTrue("Activity should finish when provisioning succeeds", activity.isFinishing());
        assertEquals(
                "Result code should be RESULT_OK so the SetupAction caller can continue",
                Activity.RESULT_OK,
                shadow.getResultCode()
        );
    }

    @Test
    public void givenMdmSetupRequestCanceled_whenOnActivityResult_thenDoesNotFinish() {
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "valid-setup-key");
        setApplicationRestrictions(restrictions);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();
        assertFalse(
                "Pre-condition: MDM splash route should not auto-finish LoginActivity",
                activity.isFinishing()
        );

        activity.onActivityResult(MDM_SETUP_REQUEST, Activity.RESULT_CANCELED, null);

        assertFalse(
                "Activity should not finish when provisioning is canceled",
                activity.isFinishing()
        );
    }

    @Test
    public void givenUnknownRequestCode_whenOnActivityResult_thenDoesNotFinish() {
        // Same setup as the RESULT_OK test so isFinishing() starts out false — this
        // isolates the effect of the unknown request code.
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "valid-setup-key");
        setApplicationRestrictions(restrictions);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        LoginActivity activity = controller.create().get();
        assertFalse(
                "Pre-condition: MDM splash route should not auto-finish LoginActivity",
                activity.isFinishing()
        );

        activity.onActivityResult(/* unknown request */ 999, Activity.RESULT_OK, null);

        assertFalse(
                "Activity should not finish for unrelated request codes",
                activity.isFinishing()
        );
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void setApplicationRestrictions(Bundle restrictions) {
        RestrictionsManager manager =
                (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
        ShadowRestrictionsManager shadow = Shadows.shadowOf(manager);
        shadow.setApplicationRestrictions(restrictions);
    }
}
