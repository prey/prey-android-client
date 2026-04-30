/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

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
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

/**
 * Robolectric tests covering the BroadcastReceiver lifecycle in
 * {@link PopUpAlertActivity}. Production previously leaked the two receivers
 * registered in {@code onCreate} because there was no matching
 * {@code unregisterReceiver} on destroy — Android logged
 * {@code IntentReceiverLeaked} every time the activity was torn down.
 *
 * <p>These tests pin the fix: receivers must be present after create and
 * absent after destroy, and {@code onDestroy} must tolerate the case where
 * one (or both) of the matching {@code registerReceiver} calls failed.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class PopUpAlertActivityRobolectricTest {

    private ShadowApplication shadowApplication;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        shadowApplication = Shadows.shadowOf(
                (Application) ApplicationProvider.getApplicationContext());
        preyConfig = PreyConfig.getPreyConfig(ApplicationProvider.getApplicationContext());
        // Keep onResume from launching LoginActivity, which would dirty the
        // started-activities queue and add noise to the assertions below.
        preyConfig.setNoficationPopupId(1);
    }

    @After
    public void tearDown() {
        preyConfig.setNoficationPopupId(0);
    }

    @Test
    public void onCreate_registersBothBroadcastReceivers() {
        ActivityController<PopUpAlertActivity> controller = buildController(7);
        try {
            controller.create();

            int registered = countReceiversForActions(
                    CheckPasswordHtmlActivity.CLOSE_PREY,
                    PopUpAlertActivity.POPUP_PREY + "_7"
            );
            assertEquals(
                    "Both close_prey and popup_prey receivers must be registered after onCreate",
                    2,
                    registered
            );
        } finally {
            controller.destroy();
        }
    }

    @Test
    public void onDestroy_unregistersBothBroadcastReceivers() {
        ActivityController<PopUpAlertActivity> controller = buildController(13);
        controller.create();
        // Sanity check: receivers are present before destroy. If this fails,
        // the post-destroy assertion below would be meaningless.
        assertEquals(
                2,
                countReceiversForActions(
                        CheckPasswordHtmlActivity.CLOSE_PREY,
                        PopUpAlertActivity.POPUP_PREY + "_13"
                )
        );

        controller.destroy();

        assertEquals(
                "Both receivers must be unregistered after onDestroy — otherwise Android logs IntentReceiverLeaked",
                0,
                countReceiversForActions(
                        CheckPasswordHtmlActivity.CLOSE_PREY,
                        PopUpAlertActivity.POPUP_PREY + "_13"
                )
        );
    }

    @Test
    public void onDestroy_isSafeWhenReceiversWereNeverRegistered() {
        // Pre-register the receivers' actions ourselves so Robolectric's
        // count starts non-zero, then create+immediately unregister our
        // sentinels. After we destroy the activity, the activity's matching
        // unregisterReceiver calls would normally throw IllegalArgumentException
        // because they were never registered against THIS activity context —
        // the production code's per-receiver try/catch must swallow that.
        ActivityController<PopUpAlertActivity> controller = buildController(99);
        controller.create();

        // Manually unregister the activity's receivers behind its back to
        // simulate the "register-failed-in-onCreate" branch. The next
        // controller.destroy() must NOT propagate IllegalArgumentException.
        for (BroadcastReceiver r : findActivityReceivers(controller.get())) {
            try {
                controller.get().unregisterReceiver(r);
            } catch (IllegalArgumentException ignored) {
                // already gone
            }
        }

        // The whole point of the fix: this must not throw.
        controller.destroy();

        assertEquals(
                "Activity must end up with no receivers regardless of how it was unwound",
                0,
                countReceiversForActions(
                        CheckPasswordHtmlActivity.CLOSE_PREY,
                        PopUpAlertActivity.POPUP_PREY + "_99"
                )
        );
    }

    @Test
    public void fullLifecycle_doesNotLeakAcrossMultipleCreateDestroyCycles() {
        // Three back-to-back launches — historically each one leaked two
        // receivers, so without the fix the registered count would only ever
        // grow.
        for (int notificationId = 1; notificationId <= 3; notificationId++) {
            ActivityController<PopUpAlertActivity> controller = buildController(notificationId);
            controller.create().destroy();
        }

        assertTrue(
                "After repeated lifecycles there should be no leftover popup receivers",
                shadowApplication.getRegisteredReceivers().stream().noneMatch(
                        wrapper -> matchesAny(wrapper.intentFilter,
                                CheckPasswordHtmlActivity.CLOSE_PREY,
                                PopUpAlertActivity.POPUP_PREY + "_1",
                                PopUpAlertActivity.POPUP_PREY + "_2",
                                PopUpAlertActivity.POPUP_PREY + "_3")
                )
        );
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private ActivityController<PopUpAlertActivity> buildController(int notificationId) {
        Bundle extras = new Bundle();
        extras.putString("alert_message", "test-message");
        extras.putInt("notificationId", notificationId);
        android.content.Intent intent = new android.content.Intent().putExtras(extras);
        return Robolectric.buildActivity(PopUpAlertActivity.class, intent);
    }

    private int countReceiversForActions(String... actions) {
        int count = 0;
        for (ShadowApplication.Wrapper wrapper : shadowApplication.getRegisteredReceivers()) {
            if (matchesAny(wrapper.intentFilter, actions)) {
                count++;
            }
        }
        return count;
    }

    private static boolean matchesAny(IntentFilter filter, String... actions) {
        if (filter == null) return false;
        for (String action : actions) {
            if (filter.hasAction(action)) return true;
        }
        return false;
    }

    private List<BroadcastReceiver> findActivityReceivers(PopUpAlertActivity activity) {
        java.util.ArrayList<BroadcastReceiver> out = new java.util.ArrayList<>();
        for (ShadowApplication.Wrapper wrapper : shadowApplication.getRegisteredReceivers()) {
            if (wrapper.context == activity) {
                out.add(wrapper.broadcastReceiver);
            }
        }
        return out;
    }
}
