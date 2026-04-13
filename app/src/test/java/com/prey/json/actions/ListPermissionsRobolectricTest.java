/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Robolectric test suite for the {@link ListPermissions} class.
 * <p>
 * Tests that getPermissions returns a well-formed JSON with all expected keys.
 * On Robolectric, runtime permissions default to denied (false) and system settings
 * like accessibility/draw_overlays default to false.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class ListPermissionsRobolectricTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void givenContext_whenGetPermissions_thenReturnsAllExpectedKeys() throws Exception {
        JSONObject permissions = ListPermissions.getPermissions(context);

        assertNotNull(permissions);
        assertTrue(permissions.has("location"));
        assertTrue(permissions.has("location_background"));
        assertTrue(permissions.has("camera"));
        assertTrue(permissions.has("notification"));
        assertTrue(permissions.has("storage"));
        assertTrue(permissions.has("draw_overlays"));
        assertTrue(permissions.has("admin"));
        assertTrue(permissions.has("accessibility"));
    }

    @Test
    public void givenContext_whenGetPermissions_thenValuesAreBooleanStrings() throws Exception {
        JSONObject permissions = ListPermissions.getPermissions(context);

        for (String key : new String[]{"location", "location_background", "camera",
                "notification", "storage", "draw_overlays", "admin", "accessibility"}) {
            String value = permissions.getString(key);
            assertTrue(
                    String.format("Key '%s' should be 'true' or 'false' but was '%s'", key, value),
                    "true".equals(value) || "false".equals(value)
            );
        }
    }

    @Test
    public void givenContext_whenGetPermissions_thenExactlyEightKeys() throws Exception {
        JSONObject permissions = ListPermissions.getPermissions(context);

        assertEquals(8, permissions.length());
    }

    @Test
    public void givenRobolectricDefaults_whenGetPermissions_thenRuntimePermissionsAreFalse() throws Exception {
        JSONObject permissions = ListPermissions.getPermissions(context);

        // On Robolectric without granted permissions, these should be false
        assertEquals("false", permissions.getString("location"));
        assertEquals("false", permissions.getString("camera"));
        assertEquals("false", permissions.getString("storage"));
    }

    @Test
    public void givenRobolectricDefaults_whenGetPermissions_thenAccessibilityIsFalse() throws Exception {
        JSONObject permissions = ListPermissions.getPermissions(context);

        assertEquals("false", permissions.getString("accessibility"));
    }

    @Test
    public void givenRobolectricDefaults_whenGetPermissions_thenAdminIsFalse() throws Exception {
        JSONObject permissions = ListPermissions.getPermissions(context);

        assertEquals("false", permissions.getString("admin"));
    }
}
