/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

/**
 * Robolectric test suite for the {@link PreyBootController} class.
 * <p>
 * Tests that a missing report restarted after boot keeps its stored settings.
 * Runs on the JVM without an emulator.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class PreyBootControllerRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        Field cached = PreyConfig.class.getDeclaredField("cachedInstance");
        cached.setAccessible(true);
        cached.set(null, null);
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMissing(false);
        preyConfig.setIntervalReport("");
        preyConfig.setExcludeReport("");
    }

    @Test
    public void givenMissingReportWithExcludedPicture_whenBootCompleted_thenExcludeIsKept() {
        preyConfig.setIntervalReport("10");
        preyConfig.setExcludeReport("picture");

        bootCompleted();

        assertEquals("picture", preyConfig.getExcludeReport());
        assertEquals("10", preyConfig.getIntervalReport());
    }

    @Test
    public void givenMissingReportWithoutExclusions_whenBootCompleted_thenNothingIsExcluded() {
        preyConfig.setIntervalReport("10");

        bootCompleted();

        assertEquals("", preyConfig.getExcludeReport());
        assertEquals("10", preyConfig.getIntervalReport());
    }

    @Test
    public void givenNoMissingReport_whenBootCompleted_thenExcludeIsUntouched() {
        preyConfig.setExcludeReport("picture");

        bootCompleted();

        assertEquals("picture", preyConfig.getExcludeReport());
        assertEquals("", preyConfig.getIntervalReport());
    }

    private void bootCompleted() {
        new PreyBootController().onReceive(context, new Intent(Intent.ACTION_BOOT_COMPLETED));
    }
}
