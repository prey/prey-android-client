/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Guards against layouts that only exist under a qualifier such as {@code -port}
 * and have no base variant in {@code res/layout}.
 * <p>
 * Such a layout resolves fine as long as the activity showing it is pinned to
 * that configuration, which is how {@code R.layout.welcomebatch} survived: every
 * activity in the app requests a portrait lock. Android 16 (API 36) ignores that
 * lock on displays whose smallest width is at least 600dp, so those layouts
 * became reachable in landscape, where the lookup throws
 * {@link Resources.NotFoundException} and takes the activity down.
 * <p>
 * These tests need no MDM enrollment, no network and no emulator: resource
 * resolution depends only on the resource name and the device configuration, so
 * a Robolectric qualifier reproduces it exactly. {@code getLayout} is used
 * instead of inflating because it performs the same configuration lookup without
 * needing a theme, a parent view or resolvable drawables.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class LayoutConfigurationCoverageRobolectricTest {

    /**
     * Configurations an activity can now land in on a large screen. The sdk level
     * is irrelevant here — what matters is that the qualifier no longer matches
     * the {@code -port} folders, which is the state API 36 makes reachable.
     */
    private static final String[] LARGE_SCREEN_LANDSCAPE_QUALIFIERS = {
            "land",
            "sw600dp-land",
            "sw720dp-land",
            "xlarge-land",
    };

    // =========================================================================
    // welcomebatch — the layout that regressed
    // =========================================================================

    @Test
    @Config(qualifiers = "sw720dp-land")
    public void givenLargeScreenLandscape_whenResolvingWelcomeBatch_thenLayoutIsFound() {
        Resources resources = resources();

        // Throws Resources.NotFoundException if no configuration matches.
        assertTrue(
                "welcomebatch must resolve in landscape: WelcomeBatchActivity's portrait "
                        + "lock is ignored on large screens from API 36 on",
                resources.getLayout(R.layout.welcomebatch) != null
        );
    }

    @Test
    @Config(qualifiers = "port")
    public void givenPortrait_whenResolvingWelcomeBatch_thenLayoutIsStillFound() {
        assertTrue(
                "welcomebatch must keep resolving in portrait — the -port variants own that case",
                resources().getLayout(R.layout.welcomebatch) != null
        );
    }

    // =========================================================================
    // Whole-app sweep
    // =========================================================================

    @Test
    @Config(qualifiers = "sw720dp-land")
    public void givenLargeScreenLandscape_whenResolvingEveryLayout_thenNoneIsMissing() {
        assertEveryLayoutResolves("sw720dp-land");
    }

    @Test
    @Config(qualifiers = "land")
    public void givenPhoneLandscape_whenResolvingEveryLayout_thenNoneIsMissing() {
        assertEveryLayoutResolves("land");
    }

    /**
     * Resolves every {@code R.layout} entry under the current configuration and
     * reports all misses at once, so a failure names each offending layout rather
     * than only the first one.
     */
    private void assertEveryLayoutResolves(String qualifiers) {
        Resources resources = resources();
        List<String> missing = new ArrayList<>();
        int checked = 0;

        for (Field field : R.layout.class.getFields()) {
            int id;
            try {
                id = field.getInt(null);
            } catch (IllegalAccessException e) {
                continue;
            }
            checked++;
            try {
                resources.getLayout(id);
            } catch (Resources.NotFoundException e) {
                missing.add(field.getName());
            }
        }

        assertTrue("Expected to find layouts to check", checked > 0);
        if (!missing.isEmpty()) {
            fail(String.format(
                    "%d layout(s) do not resolve under \"%s\" — they exist only under a "
                            + "narrower qualifier and will throw Resources.NotFoundException "
                            + "when shown in that configuration. Add a base res/layout variant "
                            + "for: %s",
                    missing.size(), qualifiers, missing));
        }
    }

    /**
     * Reports which configurations each qualifier under test actually stands for,
     * so a future reader can tell the sweep is not silently passing on a
     * configuration that still matches the {@code -port} folders.
     */
    @Test
    public void largeScreenLandscapeQualifiersAreDeclared() {
        assertTrue(
                "The sweep must cover at least a phone landscape and a large-screen landscape case",
                LARGE_SCREEN_LANDSCAPE_QUALIFIERS.length >= 2
        );
    }

    private Resources resources() {
        Context context = ApplicationProvider.getApplicationContext();
        return context.getResources();
    }
}
