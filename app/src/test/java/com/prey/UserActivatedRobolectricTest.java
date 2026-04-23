/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;

import androidx.enterprise.feedback.FakeKeyedAppStatesReporter;
import androidx.enterprise.feedback.KeyedAppState;
import androidx.test.core.app.ApplicationProvider;

import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.json.actions.UserActivated;
import com.prey.mdm.MdmKeyedAppStateReporter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class UserActivatedRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setInstallationStatus("");
    }

    @After
    public void tearDown() {
        preyConfig.setInstallationStatus("");
        MdmKeyedAppStateReporter.resetFactoryForTests();
    }

    @Test
    public void start_setsInstallationOk_emitsLinkedKeyedState_andLaunchesMainFlow() {
        FakeKeyedAppStatesReporter reporter = new FakeKeyedAppStatesReporter();
        MdmKeyedAppStateReporter.setFactoryForTests(ctx -> new MdmKeyedAppStateReporter(reporter));

        new UserActivated().start(context, null, null);

        assertEquals("OK", preyConfig.getInstallationStatus());
        assertEquals(1, reporter.getNumberOfUploads());
        KeyedAppState state = reporter.getUploadedKeyedAppStatesByKey().get(MdmKeyedAppStateReporter.SETUP_STATE_KEY);
        assertNotNull(state);
        assertEquals(MdmKeyedAppStateReporter.SETUP_STATE_DATA_LINKED, state.getData());

        ShadowApplication shadowApplication = Shadows.shadowOf((android.app.Application) context);
        Intent nextIntent = shadowApplication.getNextStartedActivity();
        assertNotNull(nextIntent);
        assertEquals(CheckPasswordHtmlActivity.class.getName(), nextIntent.getComponent().getClassName());
    }
}
