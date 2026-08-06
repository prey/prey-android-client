/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.KeyEvent;

import com.prey.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * On-device checks for the two behaviours Android 16 (API 36) changed for this app.
 * <p>
 * Run this on a display whose smallest width is at least 600dp — a tablet or foldable
 * AVD — because that is where API 36 stops honouring the portrait lock every activity
 * here requests. On a phone-sized AVD the orientation assertions still pass but prove
 * much less, so {@link #displayIsLargeEnoughToBeMeaningful()} reports the configuration
 * the run actually exercised.
 * <p>
 * These live in androidTest rather than test because they need the real resource
 * resolution and the real back dispatch, and because non-exported activities cannot be
 * started from the adb shell — instrumentation runs as the app's own UID, so it can.
 */
@RunWith(AndroidJUnit4.class)
public class LargeScreenBehaviorInstrumentedTest {

    private static final long BACK_NAVIGATION_TIMEOUT_MS = 8000;

    // =========================================================================
    // Configuration the run is actually exercising
    // =========================================================================

    /**
     * Asserts that the portrait lock really is being ignored, which is the premise every
     * other test here rests on.
     * <p>
     * PanelWebActivity declares {@code screenOrientation="portrait"} in the manifest, so
     * on a large screen running API 36 it should come up in landscape anyway. If it comes
     * up portrait, the platform is still honouring the lock and this run is not exercising
     * the change at all.
     * <p>
     * The preconditions are assumptions rather than assertions on purpose: below API 36, on
     * a phone-sized display, or with the device in portrait, this reports as skipped instead
     * of passed, so a green suite cannot be mistaken for coverage it did not provide. The
     * layout sweeps below still run either way — plain landscape is enough to catch the
     * welcomebatch class of bug, on any API level.
     */
    @Test
    public void portraitLockIsIgnoredOnThisDisplay() {
        Configuration config = resources().getConfiguration();
        int smallestWidthDp = config.smallestScreenWidthDp;

        // Below API 36 the platform still honours the lock, so the assertion below would be
        // wrong rather than merely uninformative. This matters for CI, which runs the suite
        // on an older API level too, to cover BackNavigationCompat's pre-Android 13 path.
        assumeTrue(
                String.format("API %d is below 36, where the portrait lock is still honoured",
                        Build.VERSION.SDK_INT),
                Build.VERSION.SDK_INT >= 36);
        assumeTrue(
                String.format("smallestScreenWidthDp=%d is below 600 — run on a tablet AVD to "
                        + "exercise the API 36 orientation change", smallestWidthDp),
                smallestWidthDp >= 600);
        assumeTrue(
                "Device is in portrait, so an ignored portrait lock is indistinguishable from "
                        + "an honoured one; rotate the AVD to landscape",
                config.orientation == Configuration.ORIENTATION_LANDSCAPE);

        try (ActivityScenario<PanelWebActivity> scenario =
                     ActivityScenario.launch(PanelWebActivity.class)) {
            scenario.onActivity(activity -> assertEquals(
                    String.format("PanelWebActivity asks for portrait, but on a %ddp display "
                                    + "running API 36 that request must be ignored",
                            smallestWidthDp),
                    Configuration.ORIENTATION_LANDSCAPE,
                    activity.getResources().getConfiguration().orientation));
        }
    }

    // =========================================================================
    // Resource resolution in the configuration the device is actually in
    // =========================================================================

    @Test
    public void welcomeBatchLayoutResolvesInThisConfiguration() {
        assertNotNull(
                "welcomebatch must resolve in the current configuration; it used to exist "
                        + "only under -port qualifiers, which threw once API 36 let large "
                        + "screens ignore WelcomeBatchActivity's portrait lock",
                resources().getLayout(R.layout.welcomebatch)
        );
    }

    /**
     * Name prefixes of layouts that come from AppCompat, Material and other AndroidX
     * libraries. {@code R.layout} holds the merged resources of every dependency, and some
     * library layouts are scoped to one configuration on purpose —
     * {@code material_clock_period_toggle_land} exists only for landscape, and the library
     * only reaches for it there — so sweeping them reports failures that are not ours.
     * <p>
     * Keep in sync with the copy in {@code LayoutConfigurationCoverageRobolectricTest},
     * which documents how the list was verified against the dependency set.
     */
    private static final String[] THIRD_PARTY_LAYOUT_PREFIXES = {
            "abc_", "m3_", "material_", "mtrl_", "design_", "notification_",
            "select_dialog", "support_", "preference", "browser_actions",
            "custom_dialog", "expand_button", "image_frame", "test_",
            "fingerprint_dialog", "ime_",
    };

    @Test
    public void everyLayoutResolvesInThisConfiguration() {
        Resources resources = resources();
        List<String> missing = new ArrayList<>();
        int checked = 0;
        int skipped = 0;

        for (Field field : R.layout.class.getFields()) {
            int id;
            try {
                id = field.getInt(null);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (isThirdPartyLayout(field.getName())) {
                skipped++;
                continue;
            }
            checked++;
            try {
                resources.getLayout(id);
            } catch (Resources.NotFoundException e) {
                missing.add(field.getName());
            }
        }

        assertTrue("Expected to find app-owned layouts to check", checked > 0);
        if (!missing.isEmpty()) {
            fail(String.format(
                    "%d of %d app layout(s) do not resolve on this device "
                            + "(smallestScreenWidthDp=%d, %d library layouts skipped): %s",
                    missing.size(), checked,
                    resources.getConfiguration().smallestScreenWidthDp, skipped, missing));
        }
    }

    private static boolean isThirdPartyLayout(String layoutName) {
        for (String prefix : THIRD_PARTY_LAYOUT_PREFIXES) {
            if (layoutName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // Back navigation — one activity per implementation
    // =========================================================================

    /**
     * SecurityActivity extends AppCompatActivity, so its back handling goes through
     * AndroidX's OnBackPressedDispatcher.
     */
    @Test
    public void securityActivityBackReturnsToPasswordScreen() {
        assertBackReachesPasswordScreen(SecurityActivity.class);
    }

    /**
     * PanelWebActivity extends the platform Activity, so its back handling goes through
     * BackNavigationCompat and the platform OnBackInvokedDispatcher instead. Covering
     * one activity from each family is what makes this pair worth running.
     */
    @Test
    public void panelWebActivityBackReturnsToPasswordScreen() {
        assertBackReachesPasswordScreen(PanelWebActivity.class);
    }

    /**
     * Launches {@code activityClass}, presses back, and requires that
     * CheckPasswordHtmlActivity comes up.
     * <p>
     * Asserting on the destination rather than on the launched activity finishing is
     * deliberate: the platform's default back behaviour also finishes it, so a test
     * that only checked for finishing would pass even with the back handler dead — which
     * is exactly the bug this guards.
     */
    private void assertBackReachesPasswordScreen(Class<? extends Activity> activityClass) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(
                CheckPasswordHtmlActivity.class.getName(), null, false);
        try {
            try (ActivityScenario<? extends Activity> scenario =
                         ActivityScenario.launch(activityClass)) {
                instrumentation.waitForIdleSync();
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);

                Activity destination =
                        instrumentation.waitForMonitorWithTimeout(monitor, BACK_NAVIGATION_TIMEOUT_MS);
                assertNotNull(
                        activityClass.getSimpleName() + ": back must land on "
                                + "CheckPasswordHtmlActivity. A null destination means the back "
                                + "handler never ran — the failure mode when only onBackPressed "
                                + "is overridden while enableOnBackInvokedCallback is true.",
                        destination
                );
                destination.finish();
            }
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }

    // =========================================================================
    // Not covered here: the lock screens
    // =========================================================================
    //
    // PinNativeActivity and PasswordNativeActivity are the highest-risk activities for
    // the API 36 orientation change — they declare
    // configChanges="keyboardHidden|orientation", which does not cover screenSize, so a
    // rotation destroys and recreates them, and a lock screen that does not come back is
    // an unlocked device.
    //
    // They cannot be covered by simply launching them: the app has a guard that tears
    // down a lock screen which should not be up (CloseActivity is started, and the lock
    // activity goes PAUSED -> STOPPED -> DESTROYED within about a second). That is
    // correct behaviour, so a test that launches the lock cold fails for a reason that
    // has nothing to do with rotation.
    //
    // Verifying this needs a real lock command from the panel, then a physical rotation.
    // Left as a manual check rather than a test that would pass or fail for the wrong
    // reason.

    private Resources resources() {
        Context context = ApplicationProvider.getApplicationContext();
        return context.getResources();
    }
}
