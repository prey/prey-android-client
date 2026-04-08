/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertFalse;

/**
 * Robolectric test suite for the biometric feature flag.
 * <p>
 * Validates that biometric authentication is disabled when
 * {@link PreyConfig#BIOMETRIC_ENABLED} is {@code false}.
 * Runs on the JVM without an emulator.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class BiometricFlagRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setUseBiometric(false);
    }

    // =========================================================================
    // BIOMETRIC_ENABLED flag
    // =========================================================================

    @Test
    public void givenBiometricFlagOff_thenFlagIsFalse() {
        assertFalse(PreyConfig.BIOMETRIC_ENABLED);
    }

    // =========================================================================
    // getUseBiometric gated by flag
    // =========================================================================

    @Test
    public void givenBiometricFlagOff_whenGetUseBiometric_thenReturnsFalse() {
        assertFalse(preyConfig.getUseBiometric());
    }

    @Test
    public void givenBiometricFlagOff_whenUseBiometricSetTrue_thenStillReturnsFalse() {
        preyConfig.setUseBiometric(true);
        assertFalse(preyConfig.getUseBiometric());
    }

    // =========================================================================
    // checkBiometricSupport gated by flag
    // =========================================================================

    @Test
    public void givenBiometricFlagOff_whenCheckBiometricSupport_thenReturnsFalse() {
        assertFalse(PreyPermission.checkBiometricSupport(context));
    }
}
