/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
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

import java.lang.reflect.Field;
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
    // Dialog window leak — second class of leak from the same activity:
    // popup.show() leaves the AlertDialog attached to the Activity's Window;
    // when finish() is invoked from a receiver (or anywhere else) without
    // dismissing the dialog first, Android logs WindowLeaked. The fix holds
    // the dialog as a field and dismisses it in onDestroy.
    // =========================================================================

    @Test
    public void onCreate_showsTheAlertDialog() throws Exception {
        ActivityController<PopUpAlertActivity> controller = buildController(21);
        try {
            controller.create();

            Dialog popup = readPopupField(controller.get());
            assertNotNull("Activity must hold the dialog as a field so onDestroy can dismiss it", popup);
            assertTrue(
                    "Dialog must be visible after onCreate",
                    popup.isShowing()
            );
        } finally {
            controller.destroy();
        }
    }

    @Test
    public void onDestroy_dismissesTheDialog_preventingWindowLeak() throws Exception {
        ActivityController<PopUpAlertActivity> controller = buildController(22);
        controller.create();
        Dialog popup = readPopupField(controller.get());
        assertTrue("Sanity: dialog should be showing before destroy", popup.isShowing());

        controller.destroy();

        // The fix nulls the dismiss listener and dismisses the dialog. After
        // destroy, the dialog must not be showing — otherwise Android emits
        // WindowLeaked for the still-attached DecorView.
        assertFalse(
                "Dialog must be dismissed before the Activity's window is torn down",
                popup.isShowing()
        );
    }

    @Test
    public void receiverDrivenFinish_thenDestroy_doesNotLeakDialogWindow() throws Exception {
        // This reproduces the exact field log: a popup_prey broadcast arrives,
        // the receiver calls finish(), and the lifecycle proceeds to onDestroy
        // with the dialog still attached. Without the fix the dialog would
        // remain showing and Android would log WindowLeaked.
        ActivityController<PopUpAlertActivity> controller = buildController(23);
        controller.create();
        Dialog popup = readPopupField(controller.get());
        assertTrue(popup.isShowing());

        // Simulate the broadcast that the receiver listens for. This also
        // exercises the full lifecycle: the receiver calls finish(), the
        // controller drives the destroy.
        Intent broadcast = new Intent(PopUpAlertActivity.POPUP_PREY + "_23");
        ApplicationProvider.getApplicationContext().sendBroadcast(broadcast);
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        controller.destroy();

        assertFalse(
                "Receiver-driven finish() path must still leave the dialog dismissed",
                popup.isShowing()
        );
    }

    @Test
    public void onDestroy_isSafeWhenDialogWasAlreadyDismissed() throws Exception {
        // The dismiss listener calls finish() — meaning the dismiss path can
        // already have fired by the time onDestroy runs. onDestroy must be
        // idempotent: dismissing an already-dismissed dialog is a no-op,
        // never an exception.
        ActivityController<PopUpAlertActivity> controller = buildController(24);
        controller.create();
        Dialog popup = readPopupField(controller.get());
        // Detach the dismiss listener so dismiss() doesn't recursively trigger
        // finish() before we've had a chance to drive destroy ourselves.
        popup.setOnDismissListener(null);
        popup.dismiss();
        assertFalse(popup.isShowing());

        // Must not throw.
        controller.destroy();
    }

    private static Dialog readPopupField(PopUpAlertActivity activity) throws Exception {
        Field f = PopUpAlertActivity.class.getDeclaredField("popup");
        f.setAccessible(true);
        return (Dialog) f.get(activity);
    }

    // =========================================================================
    // onResume redirect guard — every focus return used to re-fire the
    // CLEAR_TASK launch into LoginActivity, piling work onto system_server
    // exactly when focus events need to be dispatched. The guard makes the
    // redirect one-shot and finishes the activity so onResume cannot fire
    // again on the same instance.
    // =========================================================================

    @Test
    public void onResume_withZeroPopupId_redirectsExactlyOnceAndFinishes() {
        // Set popup id to 0 so the redirect branch fires.
        preyConfig.setNoficationPopupId(0);
        ActivityController<PopUpAlertActivity> controller = buildController(31);
        try {
            ShadowApplication shadowApp = Shadows.shadowOf(
                    (Application) ApplicationProvider.getApplicationContext());
            controller.create();
            // Drain the activities queued so far (the CheckPasswordHtmlActivity
            // launch is what we want to count discretely).
            drainStartedActivities(shadowApp);

            controller.start().resume();

            Intent first = shadowApp.getNextStartedActivity();
            assertNotNull(
                    "onResume must redirect to LoginActivity when popup id is 0",
                    first
            );
            assertTrue(
                    "Redirect should target LoginActivity with CLEAR_TASK semantics",
                    first.getComponent().getClassName().contains("LoginActivity")
            );
            assertEquals(
                    "Redirect must include CLEAR_TASK to wipe the popup task",
                    Intent.FLAG_ACTIVITY_CLEAR_TASK,
                    first.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            assertTrue(
                    "Activity must finish itself so onResume cannot re-fire on the same instance",
                    controller.get().isFinishing()
            );

            // Drive a second resume — under the old code this re-launched
            // LoginActivity with CLEAR_TASK every time, which was the ANR
            // amplifier. The guard must prevent it.
            controller.pause().resume();
            assertNull(
                    "Subsequent onResume must NOT fire another redirect — "
                            + "repeated CLEAR_TASK launches under load are what triggered "
                            + "the FocusEvent ANR",
                    shadowApp.getNextStartedActivity()
            );
        } finally {
            controller.destroy();
        }
    }

    @Test
    public void onResume_withNonZeroPopupId_doesNotRedirect() {
        // setUp already sets popupId to 1, so the guard branch should NOT fire.
        ActivityController<PopUpAlertActivity> controller = buildController(32);
        try {
            ShadowApplication shadowApp = Shadows.shadowOf(
                    (Application) ApplicationProvider.getApplicationContext());
            controller.create();
            drainStartedActivities(shadowApp);

            controller.start().resume();

            assertNull(
                    "onResume must not redirect while a notification popup is in progress",
                    shadowApp.getNextStartedActivity()
            );
            assertFalse(
                    "Activity must remain visible to actually show the popup",
                    controller.get().isFinishing()
            );
        } finally {
            controller.destroy();
        }
    }

    private static void drainStartedActivities(ShadowApplication shadowApp) {
        while (shadowApp.getNextStartedActivity() != null) {
            // drain queue
        }
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
