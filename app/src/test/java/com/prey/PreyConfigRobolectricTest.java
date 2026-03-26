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

import static org.junit.Assert.assertEquals;

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

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        // Clear previous values to ensure test isolation
        preyConfig.setMdmSerialNumber("");
        preyConfig.setMdmImei("");
        preyConfig.setMdmDeviceName("");
        preyConfig.setMdmOrganizationId("");
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

}
