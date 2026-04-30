/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Verifies that {@link LocationUtil#getPreyLocationAppServiceOreo} no longer
 * lets {@code BackgroundServiceStartNotAllowedException} (an
 * {@link IllegalStateException}) propagate out of the method on Android 12+.
 *
 * <p>The previous behavior dumped the full stack trace via
 * {@code PreyLogger.e} and rethrew, polluting production logs. The fix
 * catches the exception and returns null, letting the caller fall through
 * to the FusedLocationProviderClient path which doesn't need a service.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 31)
public class LocationUtilBackgroundServiceTest {

    private Context throwingContext;

    @Before
    public void setUp() {
        // Wrap the real Robolectric context so everything works EXCEPT
        // startService — which simulates the Android 12+ background restriction.
        throwingContext = new ContextWrapper(ApplicationProvider.getApplicationContext()) {
            @Override
            public android.content.ComponentName startService(Intent service) {
                throw new IllegalStateException(
                        "Not allowed to start service Intent " + service
                                + ": app is in background");
            }
        };
    }

    @Test
    public void getPreyLocationAppServiceOreo_returnsNullWhenStartServiceIsBlocked() {
        // Should NOT throw — the catch routes the BackgroundService restriction
        // into a graceful null return, which the caller treats as "use Play
        // Services fallback".
        PreyLocation result = LocationUtil.getPreyLocationAppServiceOreo(
                throwingContext, "test", false, null);

        assertNull(
                "When startService is blocked, the method must return null instead "
                        + "of letting BackgroundServiceStartNotAllowedException escape",
                result
        );
    }
}
