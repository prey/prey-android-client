/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.mdm.MdmKeyedAppStateReporter;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.enterprise.feedback.FakeKeyedAppStatesReporter;
import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Robolectric test suite for {@link SplashMdmActivity} onPostExecute branches.
 * <p>
 * The activity's {@code onCreate} inflates a ConstraintLayout-based layout that
 * can fail to inflate in headless CI environments due to theme / drawable
 * resolution. To test the completion logic independently of the UI layer, the
 * tests build a headless subclass that overrides {@code setContentView} and
 * inject stub views for the private view fields via reflection. The private
 * {@code MdmRegistrationTask} is then driven directly via reflection.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class SplashMdmActivityRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setDeviceId("");
        preyConfig.setApiKey("");
    }

    @After
    public void tearDown() {
        preyConfig.setDeviceId("");
        preyConfig.setApiKey("");
    }

    // =========================================================================
    // onPostExecute — success path (launched normally, no calling activity)
    // =========================================================================

    @Test
    public void givenRegistrationSucceededWithoutCaller_whenPostExecute_thenNavigatesToMain()
            throws Exception {
        HeadlessSplashMdmActivity activity = createHeadlessActivity();
        ShadowActivity shadow = Shadows.shadowOf(activity);
        drainStartedActivities(shadow);

        invokeOnPostExecute(activity, Boolean.TRUE);

        Intent nextIntent = shadow.getNextStartedActivity();
        assertNotNull("Should navigate to the main screen when there is no caller", nextIntent);
        assertTrue(
                "Should navigate to CheckPasswordHtmlActivity when no caller is present",
                nextIntent.getComponent().getClassName().contains("CheckPasswordHtmlActivity")
        );
        assertTrue(
                "Navigation intent should include FLAG_ACTIVITY_NEW_TASK",
                (nextIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0
        );
        assertEquals(
                "Result code should be RESULT_OK even when there is no caller",
                Activity.RESULT_OK,
                shadow.getResultCode()
        );
        assertTrue("Activity should finish after navigation", activity.isFinishing());
    }

    // =========================================================================
    // onPostExecute — success path (launched for result, e.g. SetupAction)
    // =========================================================================

    @Test
    public void givenRegistrationSucceededWithCaller_whenPostExecute_thenFinishesWithResultOkAndNoNavigation()
            throws Exception {
        HeadlessSplashMdmActivity activity = createHeadlessActivity();
        ShadowActivity shadow = Shadows.shadowOf(activity);
        drainStartedActivities(shadow);
        // Simulate being launched via startActivityForResult (LoginActivity / SetupAction).
        shadow.setCallingActivity(new ComponentName(context, LoginActivity.class));

        invokeOnPostExecute(activity, Boolean.TRUE);

        assertEquals(
                "Result code should be RESULT_OK so the caller can continue the setup flow",
                Activity.RESULT_OK,
                shadow.getResultCode()
        );
        assertTrue("Activity should finish to return control to the caller", activity.isFinishing());
        assertNull(
                "Should NOT navigate anywhere when launched for result — caller drives the flow",
                shadow.getNextStartedActivity()
        );
    }

    @Test
    public void givenRegistrationSucceededFromSetupActionWithoutCaller_whenPostExecute_thenFinishesWithResultOkAndNoNavigation()
            throws Exception {
        HeadlessSplashMdmActivity activity = createHeadlessActivity();
        ShadowActivity shadow = Shadows.shadowOf(activity);
        drainStartedActivities(shadow);
        activity.getIntent().putExtra("com.google.android.apps.work.clouddpc.EXTRA_LAUNCHED_AS_SETUP_ACTION", true);

        invokeOnPostExecute(activity, Boolean.TRUE);

        assertEquals(
                "Result code should be RESULT_OK for Android Device Policy setup actions",
                Activity.RESULT_OK,
                shadow.getResultCode()
        );
        assertTrue("Activity should finish to return control to Android Device Policy", activity.isFinishing());
        assertNull(
                "Should NOT navigate away when launched as a setup action even if there is no calling activity",
                shadow.getNextStartedActivity()
        );
    }

    @Test
    public void givenRegistrationSucceeded_whenPostExecute_thenMarksProtectReady()
            throws Exception {
        preyConfig.setProtectReady(false);
        HeadlessSplashMdmActivity activity = createHeadlessActivity();

        invokeOnPostExecute(activity, Boolean.TRUE);

        assertTrue("Successful MDM setup should mark the app as ready", preyConfig.getProtectReady());
    }

    @Test
    public void givenRegistrationSucceeded_whenPostExecute_thenEmitsLinkedKeyedState()
            throws Exception {
        FakeKeyedAppStatesReporter reporter = new FakeKeyedAppStatesReporter();
        MdmKeyedAppStateReporter.setFactoryForTests(context -> new MdmKeyedAppStateReporter(reporter));
        try {
            preyConfig.setNotificationId("push-token");
            HeadlessSplashMdmActivity activity = createHeadlessActivity();

            invokeOnPostExecute(activity, Boolean.TRUE);

            assertEquals("Splash should acknowledge setup completion to the MDM once registration succeeds", 1, reporter.getNumberOfUploads());
        } finally {
            MdmKeyedAppStateReporter.resetFactoryForTests();
        }
    }

    // =========================================================================
    // onPostExecute — failure path
    // =========================================================================

    @Test
    public void givenRegistrationFailed_whenPostExecute_thenShowsErrorAndDoesNotNavigate()
            throws Exception {
        HeadlessSplashMdmActivity activity = createHeadlessActivity();
        ShadowActivity shadow = Shadows.shadowOf(activity);
        drainStartedActivities(shadow);

        invokeOnPostExecute(activity, Boolean.FALSE);

        assertEquals(
                "Progress bar should be hidden on failure",
                View.GONE,
                activity.stubProgressBar.getVisibility()
        );
        assertEquals(
                "Status text should show the error copy",
                context.getString(com.prey.R.string.mdm_loading_error),
                activity.stubStatus.lastSetText == null ? null : activity.stubStatus.lastSetText.toString()
        );
        assertFalse("Activity should remain visible to display the error", activity.isFinishing());
        assertNull(
                "Should not navigate anywhere when registration fails",
                shadow.getNextStartedActivity()
        );
    }

    @Test
    public void givenRegistrationSucceededWithoutConfirmedPushToken_whenPostExecute_thenStillEmitsLinkedKeyedState()
            throws Exception {
        FakeKeyedAppStatesReporter reporter = new FakeKeyedAppStatesReporter();
        MdmKeyedAppStateReporter.setFactoryForTests(context -> new MdmKeyedAppStateReporter(reporter));
        try {
            preyConfig.setNotificationId("");
            HeadlessSplashMdmActivity activity = createHeadlessActivity();
            ShadowActivity shadow = Shadows.shadowOf(activity);

            invokeOnPostExecute(activity, Boolean.TRUE);

            assertEquals(1, reporter.getNumberOfUploads());
            assertEquals(Activity.RESULT_OK, shadow.getResultCode());
            assertTrue(activity.isFinishing());
        } finally {
            MdmKeyedAppStateReporter.resetFactoryForTests();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Builds a headless SplashMdmActivity that skips layout inflation so tests
     * do not depend on theme / drawable resolution that may fail in headless CI.
     */
    private HeadlessSplashMdmActivity createHeadlessActivity() throws Exception {
        ActivityController<HeadlessSplashMdmActivity> controller =
                Robolectric.buildActivity(HeadlessSplashMdmActivity.class);
        HeadlessSplashMdmActivity activity = controller.create().get();
        injectStubViews(activity);
        return activity;
    }

    /**
     * Wires the parent's private {@code textStatus}/{@code progressBar} fields to
     * the stubs held on the headless subclass so the failure path (which mutates
     * those views) has valid targets.
     */
    private void injectStubViews(HeadlessSplashMdmActivity activity) throws Exception {
        Field textStatusField = SplashMdmActivity.class.getDeclaredField("textStatus");
        textStatusField.setAccessible(true);
        textStatusField.set(activity, activity.stubStatus);

        Field progressBarField = SplashMdmActivity.class.getDeclaredField("progressBar");
        progressBarField.setAccessible(true);
        progressBarField.set(activity, activity.stubProgressBar);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void invokeOnPostExecute(SplashMdmActivity activity, Boolean result) throws Exception {
        Class<?> taskClass = Class.forName("com.prey.activities.SplashMdmActivity$MdmRegistrationTask");
        Constructor<?> ctor = taskClass.getDeclaredConstructor(SplashMdmActivity.class);
        ctor.setAccessible(true);
        AsyncTask task = (AsyncTask) ctor.newInstance(activity);

        Method onPostExecute = taskClass.getDeclaredMethod("onPostExecute", Boolean.class);
        onPostExecute.setAccessible(true);
        onPostExecute.invoke(task, result);
    }

    /**
     * Drains any activities the real {@code MdmRegistrationTask} may have queued
     * during {@code onCreate} so each test observes only its own navigation.
     */
    private void drainStartedActivities(ShadowActivity shadow) {
        while (shadow.getNextStartedActivity() != null) {
            // drain queue
        }
    }

    // =========================================================================
    // Headless test subclass + view stubs
    // =========================================================================

    /**
     * A SplashMdmActivity that skips layout inflation so tests don't need the
     * production layout, theme, or drawables to resolve under the test harness.
     */
    public static class HeadlessSplashMdmActivity extends SplashMdmActivity {

        RecordingTextView stubStatus;
        RecordingProgressBar stubProgressBar;

        @Override
        public void setContentView(int layoutResID) {
            // Intentionally skip layout inflation to keep the test headless.
            stubStatus = new RecordingTextView(this);
            stubProgressBar = new RecordingProgressBar(this);
        }
    }

    /** TextView that records the last text set via any setText(...) overload. */
    public static class RecordingTextView extends TextView {
        CharSequence lastSetText;

        RecordingTextView(Context context) {
            super(context);
        }

        // setText(int) and setText(CharSequence) are final in TextView, but they
        // both ultimately delegate to this overload, which is not final.
        @Override
        public void setText(CharSequence text, BufferType type) {
            lastSetText = text;
            // Intentionally do not call super: avoids font/paint resolution
            // that is unnecessary for what's under test.
        }
    }

    /** ProgressBar subclass — exists only so injection has a concrete type. */
    public static class RecordingProgressBar extends ProgressBar {
        RecordingProgressBar(Context context) {
            super(context);
        }
    }
}
