/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import com.prey.PreyConfig;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.receivers.PreyDeviceAdmin;

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
import org.robolectric.shadows.ShadowDevicePolicyManager;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ApplicationProvider;

/**
 * Robolectric test suite for {@link PermissionInformationActivity}.
 * <p>
 * Focuses on testing line 73: the branching logic that checks
 * {@code FroyoSupport.isAdminActive() || skipManualPermissions}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class PermissionInformationActivityRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmSkipManualPermissions(false);
        resetFroyoSupportSingleton();
    }

    @After
    public void tearDown() {
        resetFroyoSupportSingleton();
    }

    /**
     * When isAdminActive() is false and skipManualPermissions is false,
     * the activity should prompt for device admin privileges (else branch).
     */
    @Test
    public void givenNoAdminAndNoSkip_whenShowScreen_thenPromptsForAdminPrivileges() {
        preyConfig.setMdmSkipManualPermissions(false);
        setDeviceAdminActive(false);

        ActivityController<PermissionInformationActivity> controller =
                Robolectric.buildActivity(PermissionInformationActivity.class);
        PermissionInformationActivity activity = controller.create().resume().get();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        // Should have launched an activity for result (device admin prompt)
        ShadowActivity.IntentForResult intentForResult =
                shadowActivity.getNextStartedActivityForResult();
        assertNotNull("Should prompt for device admin privileges", intentForResult);
        assertEquals(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN,
                intentForResult.intent.getAction());
    }

    /**
     * When isAdminActive() is true and skipManualPermissions is false,
     * the activity should proceed past the admin check (if branch).
     */
    @Test
    public void givenAdminActiveAndNoSkip_whenShowScreen_thenProceedsPastAdminCheck() {
        preyConfig.setMdmSkipManualPermissions(false);
        setDeviceAdminActive(true);

        ActivityController<PermissionInformationActivity> controller =
                Robolectric.buildActivity(PermissionInformationActivity.class);
        PermissionInformationActivity activity = controller.create().resume().get();

        // Should NOT prompt for admin privileges — admin is already active
        // Instead it should finish and navigate forward
        assertTrue("Activity should finish when admin is active", activity.isFinishing());
    }

    /**
     * When isAdminActive() is false but skipManualPermissions is true (MDM managed),
     * the activity should skip the admin prompt and proceed (if branch via ||).
     * This is the key MDM scenario for line 73.
     */
    @Test
    public void givenNoAdminButSkipEnabled_whenShowScreen_thenSkipsAdminPrompt() {
        preyConfig.setMdmSkipManualPermissions(true);
        setDeviceAdminActive(false);

        ActivityController<PermissionInformationActivity> controller =
                Robolectric.buildActivity(PermissionInformationActivity.class);
        PermissionInformationActivity activity = controller.create().resume().get();

        // Should NOT prompt for admin — skipManualPermissions overrides
        assertTrue("Activity should finish when MDM skip is enabled", activity.isFinishing());
    }

    /**
     * When both isAdminActive() is true and skipManualPermissions is true,
     * the activity should proceed (if branch — both conditions true).
     */
    @Test
    public void givenAdminActiveAndSkipEnabled_whenShowScreen_thenProceedsPastAdminCheck() {
        preyConfig.setMdmSkipManualPermissions(true);
        setDeviceAdminActive(true);

        ActivityController<PermissionInformationActivity> controller =
                Robolectric.buildActivity(PermissionInformationActivity.class);
        PermissionInformationActivity activity = controller.create().resume().get();

        assertTrue("Activity should finish when both admin and skip are active",
                activity.isFinishing());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Uses Robolectric's ShadowDevicePolicyManager to control isAdminActive().
     */
    private void setDeviceAdminActive(boolean active) {
        DevicePolicyManager dpm = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        ShadowDevicePolicyManager shadowDpm = Shadows.shadowOf(dpm);
        ComponentName deviceAdmin = new ComponentName(context, PreyDeviceAdmin.class);
        if (active) {
            shadowDpm.setActiveAdmin(deviceAdmin);
        }
        // When active is false, the default state is inactive (no action needed)
    }

    /**
     * Resets FroyoSupport's singleton so each test gets a fresh instance
     * with the correct shadow DevicePolicyManager state.
     */
    private void resetFroyoSupportSingleton() {
        try {
            Field instanceField = FroyoSupport.class.getDeclaredField("_instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset FroyoSupport singleton", e);
        }
    }
}
