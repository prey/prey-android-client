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
import com.prey.R;

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
import java.lang.reflect.Method;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Robolectric test suite for {@link SplashMdmActivity}.
 * <p>
 * Covers:
 * <ul>
 *     <li>Initial UI state on create (loading indicator + status text).</li>
 *     <li>onPostExecute when MDM registration succeeds and the activity was launched
 *         normally (no calling activity) → navigates to the main screen.</li>
 *     <li>onPostExecute when MDM registration succeeds and the activity was launched
 *         for result (SetupAction / LoginActivity) → finishes with RESULT_OK without
 *         navigating anywhere.</li>
 *     <li>onPostExecute when MDM registration fails → error UI shown, no navigation.</li>
 * </ul>
 * <p>
 * The registration AsyncTask runs on a real background thread under Robolectric, so
 * rather than depending on async scheduling we drive the private {@code MdmRegistrationTask}
 * directly via reflection. The async entrypoint itself is exercised by the
 * {@link LoginActivityRobolectricTest} navigation tests and the
 * {@code RestrictionsReceiverRobolectricTest} unit tests.
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
    // Initial UI state
    // =========================================================================

    @Test
    public void givenActivityCreated_thenShowsLoadingUi() {
        ActivityController<SplashMdmActivity> controller =
                Robolectric.buildActivity(SplashMdmActivity.class);
        SplashMdmActivity activity = controller.create().get();

        ProgressBar progress = activity.findViewById(R.id.progress_mdm);
        TextView status = activity.findViewById(R.id.text_mdm_status);

        assertNotNull("Progress bar should be inflated", progress);
        assertNotNull("Status text should be inflated", status);
        assertEquals(
                "Progress bar should be visible during registration",
                View.VISIBLE,
                progress.getVisibility()
        );
        assertEquals(
                "Status text should display loading copy",
                context.getString(R.string.mdm_loading_title),
                status.getText().toString()
        );
    }

    // =========================================================================
    // onPostExecute — success path (launched normally, no calling activity)
    // =========================================================================

    @Test
    public void givenRegistrationSucceededWithoutCaller_whenPostExecute_thenNavigatesToMain()
            throws Exception {
        ActivityController<SplashMdmActivity> controller =
                Robolectric.buildActivity(SplashMdmActivity.class);
        SplashMdmActivity activity = controller.create().get();
        ShadowActivity shadow = Shadows.shadowOf(activity);
        // Drain anything the real AsyncTask might have queued so assertions target our call.
        drainStartedActivities(shadow);

        invokeOnPostExecute(activity, Boolean.TRUE);

        Intent nextIntent = shadow.getNextStartedActivity();
        assertNotNull("Should navigate to the main screen", nextIntent);
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
        ActivityController<SplashMdmActivity> controller =
                Robolectric.buildActivity(SplashMdmActivity.class);
        SplashMdmActivity activity = controller.create().get();
        ShadowActivity shadow = Shadows.shadowOf(activity);
        drainStartedActivities(shadow);
        // Simulate being launched via startActivityForResult (LoginActivity / SetupAction).
        shadow.setCallingActivity(
                new ComponentName(context, LoginActivity.class)
        );

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

    // =========================================================================
    // onPostExecute — failure path
    // =========================================================================

    @Test
    public void givenRegistrationFailed_whenPostExecute_thenShowsErrorAndDoesNotNavigate()
            throws Exception {
        ActivityController<SplashMdmActivity> controller =
                Robolectric.buildActivity(SplashMdmActivity.class);
        SplashMdmActivity activity = controller.create().get();
        ShadowActivity shadow = Shadows.shadowOf(activity);
        drainStartedActivities(shadow);

        invokeOnPostExecute(activity, Boolean.FALSE);

        ProgressBar progress = activity.findViewById(R.id.progress_mdm);
        TextView status = activity.findViewById(R.id.text_mdm_status);
        assertEquals(
                "Progress bar should be hidden on failure",
                View.GONE,
                progress.getVisibility()
        );
        assertEquals(
                "Status text should show the error copy",
                context.getString(R.string.mdm_loading_error),
                status.getText().toString()
        );
        assertFalse("Activity should remain visible to display the error", activity.isFinishing());
        assertNull(
                "Should not navigate anywhere when registration fails",
                shadow.getNextStartedActivity()
        );
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Invokes the private {@code MdmRegistrationTask#onPostExecute(Boolean)} directly so we
     * can deterministically exercise each completion branch without relying on the
     * real AsyncTask's background thread scheduling.
     */
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

    private void drainStartedActivities(ShadowActivity shadow) {
        while (shadow.getNextStartedActivity() != null) {
            // drain queue
        }
        while (shadow.getNextStartedActivityForResult() != null) {
            // drain queue
        }
    }
}
