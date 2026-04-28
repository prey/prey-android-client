/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAccountManager;

import androidx.test.core.app.ApplicationProvider;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Robolectric test suite for the {@link PreyConfig} class.
 * <p>
 * Tests MDM-related getters/setters and device name construction logic.
 * Runs on the JVM without an emulator.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class PreyConfigRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;
    private AccountManager accountManager;
    private ShadowAccountManager shadowAccountManager;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        // PreyConfig caches a static singleton tied to the first Context it saw.
        // Reset it so each test re-initializes against the current Robolectric
        // application context (otherwise AccountManager lookups may hit a stale ctx).
        Field cached = PreyConfig.class.getDeclaredField("cachedInstance");
        cached.setAccessible(true);
        cached.set(null, null);
        preyConfig = PreyConfig.getPreyConfig(context);
        // Clear previous values to ensure test isolation
        preyConfig.setMdmSerialNumber("");
        preyConfig.setMdmImei("");
        preyConfig.setMdmDeviceName("");
        preyConfig.setMdmOrganizationId("");
        preyConfig.setMdmSkipManualPermissions(false);
        // Register fake authenticators so addAccountExplicitly() accounts are
        // visible to getAccountsByType() under API 26+ visibility rules.
        accountManager = AccountManager.get(context);
        shadowAccountManager = Shadows.shadowOf(accountManager);
        shadowAccountManager.addAuthenticator("com.google");
        shadowAccountManager.addAuthenticator("com.microsoft.workaccount");
        clearWorkAccounts();
    }

    @After
    public void tearDown() {
        clearWorkAccounts();
    }

    private void clearWorkAccounts() {
        for (Account account : accountManager.getAccountsByType("com.google")) {
            accountManager.removeAccountExplicitly(account);
        }
        for (Account account : accountManager.getAccountsByType("com.microsoft.workaccount")) {
            accountManager.removeAccountExplicitly(account);
        }
    }

    // =========================================================================
    // MDM Serial Number
    // =========================================================================

    @Test
    public void givenSerialNumber_whenSet_thenCanBeRetrieved() {
        preyConfig.setMdmSerialNumber("SN-TEST-1234");
        assertEquals("SN-TEST-1234", preyConfig.getMdmSerialNumber());
    }

    @Test
    public void givenNoSerialNumber_whenRetrieved_thenReturnsEmptyString() {
        assertEquals("", preyConfig.getMdmSerialNumber());
    }

    @Test
    public void givenExistingSerialNumber_whenUpdated_thenNewValueIsReturned() {
        preyConfig.setMdmSerialNumber("OLD-SN");
        preyConfig.setMdmSerialNumber("NEW-SN");
        assertEquals("NEW-SN", preyConfig.getMdmSerialNumber());
    }

    // =========================================================================
    // MDM IMEI
    // =========================================================================

    @Test
    public void givenImei_whenSet_thenCanBeRetrieved() {
        preyConfig.setMdmImei("354123456789012");
        assertEquals("354123456789012", preyConfig.getMdmImei());
    }

    @Test
    public void givenNoImei_whenRetrieved_thenReturnsEmptyString() {
        assertEquals("", preyConfig.getMdmImei());
    }

    @Test
    public void givenExistingImei_whenUpdated_thenNewValueIsReturned() {
        preyConfig.setMdmImei("111111111111111");
        preyConfig.setMdmImei("222222222222222");
        assertEquals("222222222222222", preyConfig.getMdmImei());
    }

    // =========================================================================
    // MDM Device Name
    // =========================================================================

    @Test
    public void givenMdmDeviceName_whenSet_thenCanBeRetrieved() {
        preyConfig.setMdmDeviceName("Office Laptop 001");
        assertEquals("Office Laptop 001", preyConfig.getMdmDeviceName());
    }

    @Test
    public void givenNoMdmDeviceName_whenRetrieved_thenReturnsEmptyString() {
        assertEquals("", preyConfig.getMdmDeviceName());
    }

    @Test
    public void givenExistingMdmDeviceName_whenUpdated_thenNewValueIsReturned() {
        preyConfig.setMdmDeviceName("Old Name");
        preyConfig.setMdmDeviceName("New Name");
        assertEquals("New Name", preyConfig.getMdmDeviceName());
    }

    // =========================================================================
    // MDM Organization ID
    // =========================================================================

    @Test
    public void givenOrganizationId_whenSet_thenCanBeRetrieved() {
        preyConfig.setMdmOrganizationId("prey-inc");
        assertEquals("prey-inc", preyConfig.getMdmOrganizationId());
    }

    @Test
    public void givenNoOrganizationId_whenRetrieved_thenReturnsEmptyString() {
        assertEquals("", preyConfig.getMdmOrganizationId());
    }

    // =========================================================================
    // MDM Skip Manual Permissions
    // =========================================================================

    @Test
    public void givenSkipManualPermissions_whenSetTrue_thenReturnsTrue() {
        preyConfig.setMdmSkipManualPermissions(true);
        assertTrue(preyConfig.isMdmSkipManualPermissions());
    }

    @Test
    public void givenSkipManualPermissions_whenSetFalse_thenReturnsFalse() {
        preyConfig.setMdmSkipManualPermissions(false);
        assertFalse(preyConfig.isMdmSkipManualPermissions());
    }

    @Test
    public void givenNoSkipManualPermissions_whenRetrieved_thenDefaultsFalse() {
        assertFalse(preyConfig.isMdmSkipManualPermissions());
    }

    @Test
    public void givenSkipManualPermissionsTrue_whenUpdatedToFalse_thenReturnsFalse() {
        preyConfig.setMdmSkipManualPermissions(true);
        preyConfig.setMdmSkipManualPermissions(false);
        assertFalse(preyConfig.isMdmSkipManualPermissions());
    }

    // =========================================================================
    // buildDeviceName
    // =========================================================================

    @Test
    public void givenMdmDeviceNameSet_whenBuildingName_thenMdmNameIsUsed() {
        preyConfig.setMdmDeviceName("Office Phone 001");
        preyConfig.setMdmSerialNumber("R1YX100J1ON");

        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("Office Phone 001", result);
    }

    @Test
    public void givenSerialNumberOnly_whenBuildingName_thenNameIncludesSerialNumber() {
        preyConfig.setMdmSerialNumber("R1YX100J1ON");

        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("samsung SM-A145R - R1YX100J1ON", result);
    }

    @Test
    public void givenNoMdmData_whenBuildingName_thenDefaultNameIsUsed() {
        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("samsung SM-A145R", result);
    }

    @Test
    public void givenMdmDeviceNameSet_whenBuildingName_thenSerialNumberIsIgnored() {
        preyConfig.setMdmDeviceName("CEO-Phone-2026");
        preyConfig.setMdmSerialNumber("ABC123");

        String result = preyConfig.buildDeviceName("Google Pixel 9");

        assertEquals("CEO-Phone-2026", result);
    }

    // =========================================================================
    // buildDeviceName with a Google work-profile account available
    // =========================================================================

    @Test
    public void givenGoogleAccount_whenBuildingName_thenEmailIsUsed() {
        accountManager.addAccountExplicitly(new Account("user@company.com", "com.google"), null, null);

        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("user@company.com", result);
    }

    @Test
    public void givenGoogleAccountAndMdmDeviceName_whenBuildingName_thenMdmDeviceNameWins() {
        preyConfig.setMdmDeviceName("Office Phone 001");
        accountManager.addAccountExplicitly(new Account("user@company.com", "com.google"), null, null);

        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("Office Phone 001", result);
    }

    @Test
    public void givenGoogleAccountAndSerialNumber_whenBuildingName_thenSerialNumberWins() {
        preyConfig.setMdmSerialNumber("R1YX100J1ON");
        accountManager.addAccountExplicitly(new Account("user@company.com", "com.google"), null, null);

        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("samsung SM-A145R - R1YX100J1ON", result);
    }

    // =========================================================================
    // buildDeviceName with a Microsoft/Intune work account available
    // =========================================================================

    @Test
    public void givenIntuneAccount_whenBuildingName_thenEmailIsUsed() {
        accountManager.addAccountExplicitly(
                new Account("user@company.onmicrosoft.com", "com.microsoft.workaccount"), null, null);

        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("user@company.onmicrosoft.com", result);
    }

    @Test
    public void givenGoogleAndIntuneAccounts_whenBuildingName_thenGoogleAccountIsPreferred() {
        accountManager.addAccountExplicitly(new Account("user@company.com", "com.google"), null, null);
        accountManager.addAccountExplicitly(
                new Account("user@company.onmicrosoft.com", "com.microsoft.workaccount"), null, null);

        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("user@company.com", result);
    }

    @Test
    public void givenIntuneAccountAndMdmDeviceName_whenBuildingName_thenMdmDeviceNameWins() {
        preyConfig.setMdmDeviceName("Office Phone 001");
        accountManager.addAccountExplicitly(
                new Account("user@company.onmicrosoft.com", "com.microsoft.workaccount"), null, null);

        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        assertEquals("Office Phone 001", result);
    }

}
